package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.CrytpoBusiness;
import com.ai.st.microservice.workspaces.business.DatabaseIntegrationBusiness;
import com.ai.st.microservice.workspaces.business.ProviderBusiness;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceResultExportDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceRequestDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceSupplyRevisionDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRequestedDto;
import com.ai.st.microservice.workspaces.dto.providers.MicroserviceUpdateSupplyRevisionDto;
import com.ai.st.microservice.workspaces.utils.FileTool;
import com.ai.st.microservice.workspaces.utils.ZipUtil;

@Component
public class RabbitMQResultExportListener {

	@Value("${st.filesDirectory}")
	private String stFilesDirectory;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProviderBusiness providerBusiness;

	@Autowired
	private DatabaseIntegrationBusiness databaseIntegration;

	@Autowired
	private CrytpoBusiness cryptoBusiness;

	@RabbitListener(queues = "${st.rabbitmq.queueResultExport.queue}", concurrency = "${st.rabbitmq.queueResultExport.concurrency}")
	public void updateIntegration(MicroserviceResultExportDto resultDto) {

		log.info("procesando resultado de la exportación ... " + resultDto.getReference());

		try {

			String reference[] = resultDto.getReference().split("-");

			String typeResult = reference[0];

			if (typeResult.equalsIgnoreCase("export")) {

				Long supplyRequestedId = Long.parseLong(reference[1]);
				Long requestId = Long.parseLong(reference[2]);
				Long userId = Long.parseLong(reference[3]);

				MicroserviceSupplyRevisionDto supplyRevisionDto = providerBusiness
						.getSupplyRevisionFromSupplyRequested(supplyRequestedId);

				if (resultDto.getResult()) {

					// save zip file

					MicroserviceRequestDto requestDto = providerBusiness.getRequestById(requestId);

					MicroserviceSupplyRequestedDto supplyRequestedDto = requestDto.getSuppliesRequested().stream()
							.filter(sR -> sR.getId().equals(supplyRequestedId)).findAny().orElse(null);

					String urlBase = stFilesDirectory + "/" + requestDto.getMunicipalityCode().replace(" ", "_")
							+ "/insumos/proveedores/" + requestDto.getProvider().getName().replace(" ", "_") + "/"
							+ supplyRequestedDto.getTypeSupply().getName().replace(" ", "_");
					urlBase = FileTool.removeAccents(urlBase);

					String zipName = RandomStringUtils.random(20, true, false);

					Path path = Paths.get(resultDto.getPathFile());
					String originalFilename = path.getFileName().toString();
					String fileExtension = FilenameUtils.getExtension(originalFilename);
					String fileName = RandomStringUtils.random(20, true, false) + "." + fileExtension;

					String urlDocumentaryRepository = ZipUtil.zipping(new File(resultDto.getPathFile()), zipName,
							fileName, urlBase);

					// update state and URL from supply requested

					MicroserviceUpdateSupplyRequestedDto updateSupplyData = new MicroserviceUpdateSupplyRequestedDto();
					updateSupplyData.setSupplyRequestedStateId(ProviderBusiness.SUPPLY_REQUESTED_STATE_ACCEPTED);
					updateSupplyData.setUrl(urlDocumentaryRepository);
					providerBusiness.updateSupplyRequested(requestId, supplyRequestedId, updateSupplyData);

					MicroserviceUpdateSupplyRevisionDto updateRevisionData = new MicroserviceUpdateSupplyRevisionDto();
					updateRevisionData.setFinishedBy(userId);
					providerBusiness.updateSupplyRevision(supplyRequestedId, supplyRevisionDto.getId(),
							updateRevisionData);

					// delete database

//					try { 
//						databaseIntegration.dropDatabase(cryptoBusiness.decrypt(supplyRevisionDto.getDatabase()),
//								cryptoBusiness.decrypt(supplyRevisionDto.getUsername()));
//					} catch (Exception e) {
//						log.error("No se ha podido borrar la base de datos: " + e.getMessage());
//					}

					// close request
					providerBusiness.closeRequest(requestId, userId);

					// TODO: create supply

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
