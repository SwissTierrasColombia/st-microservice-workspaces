package com.ai.st.microservice.workspaces.business;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.IliFeignClient;
import com.ai.st.microservice.workspaces.drivers.PostgresDriver;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceExecuteQueryUpdateToRevisionDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIli2pgExportDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIli2pgExportReferenceDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIli2pgImportReferenceDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIlivalidatorBackgroundDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationCadastreRegistrationDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationStatDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceQueryResultRegistralRevisionDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class IliBusiness {

    @Value("${st.temporalDirectory}")
    private String stTemporalDirectory;

    @Value("${st.filesDirectory}")
    private String stFilesDirectory;

    private final Logger log = LoggerFactory.getLogger(IliBusiness.class);

    public static final Long ILI_CONCEPT_OPERATION = (long) 1;
    public static final Long ILI_CONCEPT_INTEGRATION = (long) 2;

    @Autowired
    private IliFeignClient iliClient;

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
            throw new BusinessException("No se ha podido iniciar la integración.");
        }

    }

    public void startValidation(Long requestId, String observations, String pathFile, String filenameTemporal,
                                Long supplyRequestedId, Long userCode, String modelVersion, Boolean skipGeometryValidation) throws BusinessException {

        try {

            MicroserviceIlivalidatorBackgroundDto ilivalidatorDto = new MicroserviceIlivalidatorBackgroundDto();

            ilivalidatorDto.setRequestId(requestId);
            ilivalidatorDto.setObservations(observations);
            ilivalidatorDto.setPathFile(pathFile);
            ilivalidatorDto.setFilenameTemporal(filenameTemporal);
            ilivalidatorDto.setSupplyRequestedId(supplyRequestedId);
            ilivalidatorDto.setUserCode(userCode);
            ilivalidatorDto.setVersionModel(modelVersion);
            ilivalidatorDto.setHasGeometryValidation(!skipGeometryValidation);

            iliClient.startValidation(ilivalidatorDto);

        } catch (Exception e) {
            throw new BusinessException("No se ha podido iniciar la validación.");
        }

    }

    public MicroserviceIntegrationStatDto getIntegrationStats(String databaseHost, String databasePort,
                                                              String databaseName, String databaseUsername, String databasePassword, String databaseSchema) {

        PostgresDriver connection = new PostgresDriver();

        String urlConnection = "jdbc:postgresql://" + databaseHost + ":" + databasePort + "/" + databaseName;
        connection.connect(urlConnection, databaseUsername, databasePassword, "org.postgresql.Driver");

        String sqlCountSNR = "SELECT count(*) FROM " + databaseSchema + ".snr_predio_juridico;";
        long countSNR = connection.count(sqlCountSNR);

        String sqlCountGC = "SELECT count(*) FROM " + databaseSchema + ".gc_predio_catastro;";
        long countGC = connection.count(sqlCountGC);

        String sqlCountMatch = "SELECT count(*) FROM " + databaseSchema + ".ini_predio_insumos;";
        long countMatch = connection.count(sqlCountMatch);

        double percentage;

        if (countSNR >= countGC) {
            percentage = (double) (countMatch * 100) / countSNR;
        } else {
            percentage = (double) (countMatch * 100) / countGC;
        }

        connection.disconnect();

        MicroserviceIntegrationStatDto integrationStat = new MicroserviceIntegrationStatDto();
        integrationStat.setCountGC(countGC);
        integrationStat.setCountSNR(countSNR);
        integrationStat.setCountMatch(countMatch);
        integrationStat.setPercentage(percentage);

        return integrationStat;
    }

    public void startImport(String pathFile, String hostname, String database, String password, String port,
                            String schema, String username, String reference, String versionModel, Long conceptId)
            throws BusinessException {

        try {
            MicroserviceIli2pgImportReferenceDto importDto = new MicroserviceIli2pgImportReferenceDto();

            importDto.setPathXTF(pathFile);
            importDto.setDatabaseHost(hostname);
            importDto.setDatabaseName(database);
            importDto.setDatabasePassword(password);
            importDto.setDatabasePort(port);
            importDto.setDatabaseSchema(schema);
            importDto.setDatabaseUsername(username);
            importDto.setReference(reference);
            importDto.setVersionModel(versionModel);
            importDto.setConceptId(conceptId);

            iliClient.startImport(importDto);

        } catch (Exception e) {
            throw new BusinessException("No se ha podido iniciar la impotación.");
        }

    }

    public MicroserviceQueryResultRegistralRevisionDto getResultQueryRegistralRevision(String host, String database,
                                                                                       String password, String port, String schema, String username, String versionModel, int page, int limit) {

        MicroserviceQueryResultRegistralRevisionDto resultDto = null;

        try {
            resultDto = iliClient.getRecordsFromQueryRegistralRevision(host, database, schema, port, username, password,
                    versionModel, IliBusiness.ILI_CONCEPT_INTEGRATION, page, limit);
        } catch (Exception e) {
            log.error("No se ha podido realizar la consulta: " + e.getMessage());
        }

        return resultDto;
    }

    public void updateRecordFromRevision(String host, String database, String password, String port, String schema,
                                         String username, String versionModel, Long boundarySpaceId, Long entityId, String namespace,
                                         String urlFile) {

        try {

            MicroserviceExecuteQueryUpdateToRevisionDto data = new MicroserviceExecuteQueryUpdateToRevisionDto();
            data.setBoundarySpaceId(boundarySpaceId);
            data.setConceptId(IliBusiness.ILI_CONCEPT_INTEGRATION);
            data.setDatabaseHost(host);
            data.setDatabaseName(database);
            data.setDatabasePassword(password);
            data.setDatabasePort(port);
            data.setDatabaseSchema(schema);
            data.setDatabaseUsername(username);
            data.setEntityId(entityId);
            data.setNamespace(namespace);
            data.setUrlFile(urlFile);
            data.setVersionModel(versionModel);

            iliClient.updateRecordFromRevision(data);
        } catch (Exception e) {
            log.error("No se ha podido realizar la actualizacion del registro: " + e.getMessage());
        }
    }

    public void startExportReference(String pathFile, String hostname, String database, String password, String port,
                                     String schema, String username, String reference, String versionModel, Long conceptId)
            throws BusinessException {

        try {
            MicroserviceIli2pgExportReferenceDto exportDto = new MicroserviceIli2pgExportReferenceDto();

            exportDto.setPathFileXTF(pathFile);
            exportDto.setDatabaseHost(hostname);
            exportDto.setDatabaseName(database);
            exportDto.setDatabasePassword(password);
            exportDto.setDatabasePort(port);
            exportDto.setDatabaseSchema(schema);
            exportDto.setDatabaseUsername(username);
            exportDto.setReference(reference);
            exportDto.setVersionModel(versionModel);
            exportDto.setConceptId(conceptId);

            iliClient.startExportReference(exportDto);

        } catch (Exception e) {
            log.error("No se ha podido iniciar la exportacion de la base de datos: " + e.getMessage());
            throw new BusinessException("No se ha podido iniciar la exportación.");
        }

    }

}
