package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.StateEntity;
import com.ai.st.microservice.workspaces.repositories.StateRepository;

@Service
public class StateService implements IStateService {

	@Autowired
	private StateRepository stateRepository;

	@Override
	public Long getCount() {
		return stateRepository.count();
	}

	@Override
	@Transactional
	public StateEntity createState(StateEntity stateEntity) {
		return stateRepository.save(stateEntity);
	}

	@Override
	public StateEntity getStateById(Long id) {
		return stateRepository.findById(id).orElse(null);
	}

}
