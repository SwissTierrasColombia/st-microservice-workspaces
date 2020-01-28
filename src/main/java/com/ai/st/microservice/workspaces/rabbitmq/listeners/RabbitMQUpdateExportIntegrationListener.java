package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.business.SupplyBusiness;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIliExportResultDto;
import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;
import com.ai.st.microservice.workspaces.services.IntegrationService;

@Component
public class RabbitMQUpdateExportIntegrationListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private IntegrationService integrationService;

	@RabbitListener(queues = "${st.rabbitmq.queueUpdateExport.queue}", concurrency = "${st.rabbitmq.queueUpdateExport.concurrency}")
	public void updateExport(MicroserviceIliExportResultDto resultExportDto) {

		try {

			Long stateId = null;

			if (resultExportDto.isStatus()) {
				stateId = IntegrationStateBusiness.STATE_GENERATED_PRODUCT;
				log.info("Export finished successful");

				// TODO: Notify to file manager for move the file
				List<String> urlsAttachments = new ArrayList<>();
				urlsAttachments.add("url");

				// load supply to municipality

				IntegrationEntity integrationEntity = integrationService
						.getIntegrationById(resultExportDto.getIntegrationId());

				if (integrationEntity instanceof IntegrationEntity) {

					WorkspaceEntity workspaceEntity = integrationEntity.getWorkspace();
					String observations = "Archivo XTF generado para el modelo de insumos";
					supplyBusiness.createSupply(workspaceEntity.getMunicipality().getCode(), observations, null,
							urlsAttachments, null, null, null, workspaceEntity.getManagerCode());

				}

			} else {
				stateId = IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT;
				log.info("Export finished with errors");
			}

			integrationBusiness.updateStateToIntegration(resultExportDto.getIntegrationId(), stateId, null, null,
					"SISTEMA");

		} catch (Exception e) {
			log.info("Error update export integration: " + e.getMessage());
		}

	}

}
