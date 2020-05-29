package com.ai.st.microservice.workspaces.business;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.IliFeignClient;
import com.ai.st.microservice.workspaces.drivers.PostgresDriver;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIli2pgExportDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIlivalidatorBackgroundDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationCadastreRegistrationDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationStatDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class IliBusiness {

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

	@Value("${st.filesDirectory}")
	private String stFilesDirectory;

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
			Long supplyRequestedId, Long userCode, String modelVersion) throws BusinessException {

		try {

			MicroserviceIlivalidatorBackgroundDto ilivalidatorDto = new MicroserviceIlivalidatorBackgroundDto();

			ilivalidatorDto.setRequestId(requestId);
			ilivalidatorDto.setObservations(observations);
			ilivalidatorDto.setPathFile(pathFile);
			ilivalidatorDto.setFilenameTemporal(filenameTemporal);
			ilivalidatorDto.setSupplyRequestedId(supplyRequestedId);
			ilivalidatorDto.setUserCode(userCode);
			ilivalidatorDto.setVersionModel(modelVersion);

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

		double percentage = 0.0;

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

}
