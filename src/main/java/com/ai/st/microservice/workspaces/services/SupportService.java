package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.SupportEntity;
import com.ai.st.microservice.workspaces.repositories.SupportRepository;

@Service
public class SupportService implements ISupportService {
	
	@Autowired
	private SupportRepository supportRepository;

	@Override
	@Transactional
	public SupportEntity createSupport(SupportEntity entity) {
		return supportRepository.save(entity);
	}

}
