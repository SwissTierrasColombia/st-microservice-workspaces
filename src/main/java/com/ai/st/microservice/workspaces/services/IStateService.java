package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.StateEntity;

public interface IStateService {

	public Long getCount();

	public StateEntity createState(StateEntity stateEntity);

	public StateEntity getStateById(Long id);

}
