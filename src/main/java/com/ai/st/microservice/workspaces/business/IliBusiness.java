package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.clients.IliFeignClient;
import com.ai.st.microservice.common.dto.ili.*;
import com.ai.st.microservice.common.exceptions.BusinessException;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class IliBusiness {

    @Value("${st.temporalDirectory}")
    private String stTemporalDirectory;

    @Value("${st.filesDirectory}")
    private String stFilesDirectory;

    private final Logger log = LoggerFactory.getLogger(IliBusiness.class);

    public static final Long ILI_CONCEPT_OPERATION = (long) 1;
    public static final Long ILI_CONCEPT_INTEGRATION = (long) 2;

    private final IliFeignClient iliClient;

    public IliBusiness(IliFeignClient iliClient) {
        this.iliClient = iliClient;
    }

    public void startExport(String hostname, String database, String password, String port, String schema,
            String username, Long integrationId, Boolean withStats, String modelVersion, String namespace)
            throws BusinessException {

        try {

            MicroserviceIli2pgExportDto exportDto = new MicroserviceIli2pgExportDto();

            exportDto.setDatabaseHost(hostname);
            exportDto.setDatabaseName(database);
            exportDto.setDatabasePassword(password);
            exportDto.setDatabasePort(port);
            exportDto.setDatabaseSchema(schema);
            exportDto.setDatabaseUsername(username);
            exportDto.setIntegrationId(integrationId);
            exportDto.setWithStats(withStats);
            exportDto.setVersionModel(modelVersion);

            String randomFilename = RandomStringUtils.random(20, true, false).toLowerCase();
            exportDto.setPathFileXTF(stFilesDirectory + namespace + File.separator + randomFilename + ".xtf");

            iliClient.startExport(exportDto);

        } catch (Exception e) {
            log.error(String.format("Error starting export %s", e.getMessage()));
            throw new BusinessException("No se ha podido iniciar la generación del insumo");
        }

    }

    public void startIntegration(String pathFileCadastre, String pathFileRegistration, String hostname, String database,
            String password, String port, String schema, String username, Long integrationId, String versionModel)
            throws BusinessException {

        try {
            MicroserviceIntegrationCadastreRegistrationDto integrationDto = new MicroserviceIntegrationCadastreRegistrationDto();

            integrationDto.setCadastrePathXTF(pathFileCadastre);
            integrationDto.setRegistrationPathXTF(pathFileRegistration);
            integrationDto.setDatabaseHost(hostname);
            integrationDto.setDatabaseName(database);
            integrationDto.setDatabasePassword(password);
            integrationDto.setDatabasePort(port);
            integrationDto.setDatabaseSchema(schema);
            integrationDto.setDatabaseUsername(username);
            integrationDto.setIntegrationId(integrationId);
            integrationDto.setVersionModel(versionModel);

            iliClient.startIntegrationCadastreRegistration(integrationDto);

        } catch (Exception e) {
            log.error(String.format("Error starting integration %s", e.getMessage()));
            throw new BusinessException("No se ha podido iniciar la integración.");
        }

    }

    public void startValidation(Long requestId, String observations, String pathFile, Long supplyRequestedId,
            Long userCode, String modelVersion, Boolean skipGeometryValidation, Boolean skipErrors)
            throws BusinessException {

        try {

            MicroserviceIlivalidatorBackgroundDto ilivalidatorDto = new MicroserviceIlivalidatorBackgroundDto();

            ilivalidatorDto.setRequestId(requestId);
            ilivalidatorDto.setObservations(observations);
            ilivalidatorDto.setPathFile(pathFile);
            ilivalidatorDto.setSupplyRequestedId(supplyRequestedId);
            ilivalidatorDto.setUserCode(userCode);
            ilivalidatorDto.setVersionModel(modelVersion);
            ilivalidatorDto.setSkipGeometryValidation(skipGeometryValidation);
            ilivalidatorDto.setSkipErrors(skipErrors);
            ilivalidatorDto.setConceptId(IliBusiness.ILI_CONCEPT_OPERATION);
            ilivalidatorDto.setQueueResponse("QUEUE_UPDATE_STATE_XTF_SUPPLIES");

            iliClient.startValidation(ilivalidatorDto);

        } catch (Exception e) {
            log.error(String.format("Error starting validation %s", e.getMessage()));
            throw new BusinessException("No se ha podido iniciar la validación.");
        }

    }

}
