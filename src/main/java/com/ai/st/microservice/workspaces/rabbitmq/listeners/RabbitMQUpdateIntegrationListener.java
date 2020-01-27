package com.ai.st.microservice.workspaces.rabbitmq.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.IntegrationBusiness;
import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.dto.ili.MicroserviceIntegrationStatDto;

@Component
public class RabbitMQUpdateIntegrationListener {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IntegrationBusiness integrationBusiness;

	@RabbitListener(queues = "${st.rabbitmq.queueUpdateIntegration.queue}", concurrency = "${st.rabbitmq.queueUpdateIntegration.concurrency}")
	public void updateIntegration(MicroserviceIntegrationStatDto integrationStats) {

		try {

			Long stateId = null;

			if (integrationStats.isStatus()) {
				integrationBusiness.addStatToIntegration(integrationStats.getIntegrationId(),
						integrationStats.getCountSNR(), integrationStats.getCountGC(), (long) 0,
						integrationStats.getCountMatch(), integrationStats.getPercentage());
				stateId = IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC;
				log.info("Integration automatic finished successful");
			} else {
				stateId = IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC;
				log.info("Integration automatic finished with errors");
			}

			integrationBusiness.updateStateToIntegration(integrationStats.getIntegrationId(), stateId, null, null,
					"SISTEMA");

		} catch (Exception e) {
			log.error("Error RabbitMQUpdateIntegrationListener@updateIntegration#Business ---> " + e.getMessage());
		}

	}

}
