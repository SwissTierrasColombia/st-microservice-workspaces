package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.SupportEntity;

public interface ISupportService {
	
	public SupportEntity createSupport(SupportEntity entity);
	
	public void deleteSupportById(Long supportId);

}
