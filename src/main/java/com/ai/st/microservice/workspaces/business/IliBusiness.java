package com.ai.st.microservice.workspaces.business;

import java.io.File;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.IliFeignClient;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIli2pgExportDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIlivalidatorBackgroundDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationCadastreRegistrationDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Component
public class IliBusiness {

	@Value("${st.temporalDirectory}")
	private String stTemporalDirectory;

	@Autowired
	private IliFeignClient iliClient;

	public void startExport(String hostname, String database, String password, String port, String schema,
			String username, Long integrationId) throws BusinessException {

		try {

			MicroserviceIli2pgExportDto exportDto = new MicroserviceIli2pgExportDto();

			exportDto.setDatabaseHost(hostname);
			exportDto.setDatabaseName(database);
			exportDto.setDatabasePassword(password);
			exportDto.setDatabasePort(port);
			exportDto.setDatabaseSchema(schema);
			exportDto.setDatabaseUsername(username);
			exportDto.setIntegrationId(integrationId);

			String randomFilename = RandomStringUtils.random(15, true, false).toLowerCase();
			exportDto.setPathFileXTF(stTemporalDirectory + File.separator + randomFilename + ".xtf");

			iliClient.startExport(exportDto);

		} catch (Exception e) {
			throw new BusinessException("No se ha podido iniciar la generación del insumo");
		}

	}

	public void startIntegration(String pathFileCadastre, String pathFileRegistration, String hostname, String database,
			String password, String port, String schema, String username, Long integrationId) throws BusinessException {

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

			iliClient.startIntegrationCadastreRegistration(integrationDto);

		} catch (Exception e) {
			throw new BusinessException("No se ha podido iniciar la integración.");
		}

	}

	public void startValidation(Long requestId, String observations, String pathFile, String filenameTemporal,
			Long supplyRequestedId, Long userCode) throws BusinessException {

		try {

			MicroserviceIlivalidatorBackgroundDto ilivalidatorDto = new MicroserviceIlivalidatorBackgroundDto();

			ilivalidatorDto.setRequestId(requestId);
			ilivalidatorDto.setObservations(observations);
			ilivalidatorDto.setPathFile(pathFile);
			ilivalidatorDto.setFilenameTemporal(filenameTemporal);
			ilivalidatorDto.setSupplyRequestedId(supplyRequestedId);
			ilivalidatorDto.setUserCode(userCode);

			iliClient.startValidation(ilivalidatorDto);

		} catch (Exception e) {
			throw new BusinessException("No se ha podido iniciar la validación.");
		}

	}

}
