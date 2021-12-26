package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import com.ai.st.microservice.common.dto.ili.MicroserviceResultExportDto;
import com.ai.st.microservice.common.dto.providers.*;
import com.ai.st.microservice.common.dto.supplies.MicroserviceCreateSupplyAttachmentDto;

import com.ai.st.microservice.workspaces.dto.providers.*;
import com.ai.st.microservice.workspaces.business.CrytpoBusiness;
import com.ai.st.microservice.workspaces.business.DatabaseIntegrationBusiness;
import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RabbitMQResultExportListener {

    @Value("${st.filesDirectory}")
    private String stFilesDirectory;

    @Value("${st.ftp.host}")
    private String hostFTP;

    @Value("${st.ftp.port}")
    private int portFTP;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ProviderBusiness providerBusiness;
    private final DatabaseIntegrationBusiness databaseIntegration;
    private final CrytpoBusiness cryptoBusiness;
    private final SupplyBusiness supplyBusiness;

    public RabbitMQResultExportListener(ProviderBusiness providerBusiness, DatabaseIntegrationBusiness databaseIntegration,
                                        CrytpoBusiness cryptoBusiness, SupplyBusiness supplyBusiness) {
        this.providerBusiness = providerBusiness;
        this.databaseIntegration = databaseIntegration;
        this.cryptoBusiness = cryptoBusiness;
        this.supplyBusiness = supplyBusiness;
    }

    @RabbitListener(queues = "${st.rabbitmq.queueResultExport.queue}", concurrency = "${st.rabbitmq.queueResultExport.concurrency}")
    public void updateIntegration(MicroserviceResultExportDto resultDto) {

        log.info("procesando resultado de la exportación ... " + resultDto.getReference());

        try {

            String[] reference = resultDto.getReference().split("-");

            String typeResult = reference[0];

            if (typeResult.equalsIgnoreCase("export")) {

                Long supplyRequestedId = Long.parseLong(reference[1]);
                Long requestId = Long.parseLong(reference[2]);
                Long userId = Long.parseLong(reference[3]);

                MicroserviceSupplyRevisionDto supplyRevisionDto = providerBusiness
                        .getSupplyRevisionFromSupplyRequested(supplyRequestedId);

                if (resultDto.getResult()) {

                    // save zip file

                    CustomRequestDto requestDto = providerBusiness.getRequestById(requestId);

                    List<? extends MicroserviceSupplyRequestedDto> suppliesResponse = requestDto.getSuppliesRequested();
                    List<CustomSupplyRequestedDto> suppliesRequestDto =
                            suppliesResponse.stream().map(CustomSupplyRequestedDto::new).collect(Collectors.toList());

                    CustomSupplyRequestedDto supplyRequestedDto = suppliesRequestDto.stream()
                            .filter(sR -> sR.getId().equals(supplyRequestedId)).findAny().orElse(null);

                    String urlDocumentaryRepository = resultDto.getPathFile();

                    log.info("url file (snr export): " + urlDocumentaryRepository);

                    // update state and URL from supply requested

                    MicroserviceUpdateSupplyRequestedDto updateSupplyData = new MicroserviceUpdateSupplyRequestedDto();
                    updateSupplyData.setSupplyRequestedStateId(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED);
                    updateSupplyData.setUrl(urlDocumentaryRepository);
                    providerBusiness.updateSupplyRequested(requestId, supplyRequestedId, updateSupplyData);

                    MicroserviceUpdateSupplyRevisionDto updateRevisionData = new MicroserviceUpdateSupplyRevisionDto();
                    updateRevisionData.setFinishedBy(userId);
                    providerBusiness.updateSupplyRevision(supplyRequestedId, supplyRevisionDto.getId(),
                            updateRevisionData);

                    // close request
                    providerBusiness.closeRequest(requestId, userId);

                    // create supply

                    List<MicroserviceCreateSupplyAttachmentDto> attachments = new ArrayList<>();

                    attachments.add(new MicroserviceCreateSupplyAttachmentDto(urlDocumentaryRepository,
                            SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_SUPPLY));

                    String ftpData = null;
                    try {
                        ftpData = "Servidor: " + hostFTP + " Puerto: " + portFTP + " Usuario: "
                                + cryptoBusiness.decrypt(supplyRevisionDto.getUsername()) + " Contraseña: "
                                + cryptoBusiness.decrypt(supplyRevisionDto.getPassword());
                    } catch (Exception e) {
                        log.error("Error creando información FTP: " + e.getMessage());
                    }

                    attachments.add(new MicroserviceCreateSupplyAttachmentDto(ftpData,
                            SupplyBusiness.SUPPLY_ATTACHMENT_TYPE_FTP));

                    List<? extends MicroserviceEmitterDto> emittersResponse = requestDto.getEmitters();
                    List<CustomEmitterDto> emittersRequestDto =
                            emittersResponse.stream().map(CustomEmitterDto::new).collect(Collectors.toList());

                    CustomEmitterDto emitterDto = emittersRequestDto.stream().
                            filter(e -> e.getEmitterType().equalsIgnoreCase("ENTITY")).findAny().orElse(null);

                    supplyBusiness.createSupply(requestDto.getMunicipalityCode(), supplyRequestedDto.getObservations(),
                            supplyRequestedDto.getTypeSupply().getId(), emitterDto.getEmitterCode(), attachments, requestId, userId,
                            requestDto.getProvider().getId(), null, null, supplyRequestedDto.getModelVersion(),
                            SupplyBusiness.SUPPLY_STATE_ACTIVE, supplyRequestedDto.getTypeSupply().getName(), supplyRequestedDto.getValid());

                    // delete database
                    try {
                        databaseIntegration.dropDatabase(cryptoBusiness.decrypt(supplyRevisionDto.getDatabase()),
                                cryptoBusiness.decrypt(supplyRevisionDto.getUsername()));
                    } catch (Exception e) {
                        log.error("No se ha podido borrar la base de datos: " + e.getMessage());
                    }

                } else {

                    providerBusiness.updateStateToSupplyRequested(requestId, supplyRequestedId,
                            ProviderBusiness.SUPPLY_REQUESTED_STATE_IN_REVIEW);

                }

                log.info("se realizaron los procesos del resultado: " + resultDto.getResult());

            }

        } catch (Exception e) {
            log.error("Ha ocurrido un error actualizando el resultado de la exportación: " + e.getMessage());
        }

    }

}
