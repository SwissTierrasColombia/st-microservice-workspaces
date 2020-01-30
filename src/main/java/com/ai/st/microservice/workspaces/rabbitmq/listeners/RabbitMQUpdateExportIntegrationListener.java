package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIliExportResultDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IntegrationService;
import com.ai.st.microservice.workspaces.services.RabbitMQSenderService;

@Component
public class RabbitMQUpdateExportIntegrationListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IntegrationBusiness integrationBusiness;

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

			if (resultExportDto.isStatus()) {
				stateId = IntegrationStateBusiness.STATE_GENERATED_PRODUCT;
				log.info("Export finished successful");

				IntegrationEntity integrationEntity = integrationService
						.getIntegrationById(resultExportDto.getIntegrationId());

				if (integrationEntity instanceof IntegrationEntity) {

					WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();
					String municipalityCode = workspaceEntity.getMunicipality().getCode();

					// notify to file manager for move the file
					String urlBase = "/" + municipalityCode.replace(" ", "_") + "/insumos/gestores/"
							+ workspaceEntity.getManagerCode();

					Path path = Paths.get(resultExportDto.getPathFile());
					String fileName = path.getFileName().toString();

					String urlDocumentaryRepository = rabbitMQService.sendFile(StringUtils.cleanPath(fileName),
							urlBase);

					List<String> urlsAttachments = new ArrayList<>();
					urlsAttachments.add(urlDocumentaryRepository);

					// load supply to municipality
					String observations = "Archivo XTF generado para el modelo de insumos";
					supplyBusiness.createSupply(municipalityCode, observations, null, urlsAttachments, null, null, null,
							null, workspaceEntity.getManagerCode());

					try {
						// delete database
						databaseIntegration.dropDatabase(cryptoBusiness.decrypt(integrationEntity.getDatabase()));
					} catch (Exception e) {
						log.error("No se ha podido borrar la base de datos: " + e.getMessage());
					}

				}

			} else {
				stateId = IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT;
				log.error("Export finished with errors");
			}

			integrationBusiness.updateStateToIntegration(resultExportDto.getIntegrationId(), stateId, null, null,
					"SISTEMA");

		} catch (Exception e) {
			log.info("Error update export integration: " + e.getMessage());
		}

	}

}
