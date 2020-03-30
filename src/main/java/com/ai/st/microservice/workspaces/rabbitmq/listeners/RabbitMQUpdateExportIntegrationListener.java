package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ai.st.microservice.workspaces.business.CrytpoBusiness;
import com.ai.st.microservice.workspaces.business.DatabaseIntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.business.ManagerBusiness;
import com.ai.st.microservice.workspaces.business.NotificationBusiness;
import com.ai.st.microservice.workspaces.business.RoleBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.business.UserBusiness;
import com.ai.st.microservice.workspaces.dto.administration.MicroserviceUserDto;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIliExportResultDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerUserDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IntegrationService;
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

@Component
public class RabbitMQUpdateExportIntegrationListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@Autowired
	private ManagerBusiness managerBusiness;

	@Autowired
	private UserBusiness userBusiness;

	@Autowired
	private NotificationBusiness notificationBusiness;

	@Autowired
	private DatabaseIntegrationBusiness databaseIntegration;

	@Autowired
	private CrytpoBusiness cryptoBusiness;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private IntegrationService integrationService;

	@Autowired
	private RabbitMQSenderService rabbitMQService;

	@RabbitListener(queues = "${st.rabbitmq.queueUpdateExport.queue}", concurrency = "${st.rabbitmq.queueUpdateExport.concurrency}")
	public void updateExport(MicroserviceIliExportResultDto resultExportDto) {

		try {

			Long stateId = null;

			IntegrationEntity integrationEntity = integrationService
					.getIntegrationById(resultExportDto.getIntegrationId());

			if (integrationEntity instanceof IntegrationEntity) {

				WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();
				MunicipalityEntity municipalityEntity = workspaceEntity.getMunicipality();

				if (resultExportDto.isStatus()) {
					stateId = IntegrationStateBusiness.STATE_GENERATED_PRODUCT;
					log.info("Export finished successful");

					if (resultExportDto.getStats() != null) {
						integrationBusiness.addStatToIntegration(integrationEntity.getId(),
								resultExportDto.getStats().getCountSNR(), resultExportDto.getStats().getCountGC(),
								(long) 0, resultExportDto.getStats().getCountMatch(),
								resultExportDto.getStats().getPercentage());
					}

					String municipalityCode = municipalityEntity.getCode();

					// notify to file manager for move the file
					String urlBase = "/" + municipalityCode.replace(" ", "_") + "/insumos/gestores/"
							+ workspaceEntity.getManagerCode();

					Path path = Paths.get(resultExportDto.getPathFile());
					String fileName = path.getFileName().toString();

					String urlDocumentaryRepository = rabbitMQService.sendFile(StringUtils.cleanPath(fileName), urlBase,
							true);

					log.info("saving url file: " + urlDocumentaryRepository);

					List<String> urlsAttachments = new ArrayList<>();
					urlsAttachments.add(urlDocumentaryRepository);

					// load supply to municipality
					String observations = "Archivo XTF generado para el modelo de insumos";
					supplyBusiness.createSupply(municipalityCode, observations, null, urlsAttachments, null, null, null,
							null, workspaceEntity.getManagerCode(), resultExportDto.getModelVersion());

					try {
						// delete database
						databaseIntegration.dropDatabase(cryptoBusiness.decrypt(integrationEntity.getDatabase()),
								cryptoBusiness.decrypt(integrationEntity.getUsername()));
					} catch (Exception e) {
						log.error("No se ha podido borrar la base de datos: " + e.getMessage());
					}

					// send notification

					try {

						List<MicroserviceManagerUserDto> directors = managerBusiness.getUserByManager(
								workspaceEntity.getManagerCode(),
								new ArrayList<Long>(Arrays.asList(RoleBusiness.SUB_ROLE_DIRECTOR)));

						for (MicroserviceManagerUserDto directorDto : directors) {

							MicroserviceUserDto userDto = userBusiness.getUserById(directorDto.getUserCode());
							if (userDto instanceof MicroserviceUserDto) {
								notificationBusiness.sendNotificationProductGenerated(userDto.getEmail(),
										userDto.getId(), municipalityEntity.getName(),
										municipalityEntity.getDepartment().getName(), new Date());
							}

						}

					} catch (Exception e) {
						log.error("Error enviando notificación de producto generado: " + e.getMessage());
					}

				} else {
					stateId = IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT;
					log.error("Export finished with errors");
				}

				integrationBusiness.updateStateToIntegration(resultExportDto.getIntegrationId(), stateId, null, null,
						"SISTEMA");

			}

		} catch (Exception e) {
			log.info("Error update export integration: " + e.getMessage());
		}

	}

}
