package com.ai.st.microservice.workspaces.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.services.ISupportService;

@Component
public class SupportBusiness {

	private final Logger log = LoggerFactory.getLogger(SupportBusiness.class);

	@Autowired
	private ISupportService supportService;

	public void deleteSupportById(Long supportId) {
		try {
			supportService.deleteSupportById(supportId);
		} catch (Exception e) {
			log.error("No se podido borrar el soporte.");
		}
	}

}
