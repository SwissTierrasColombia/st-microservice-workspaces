package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.repositories.MilestoneRepository;

@Service
public class MilestoneService implements IMilestoneService {

	@Autowired
	private MilestoneRepository milestoneRepository;

	@Override
	public Long getCount() {
		return milestoneRepository.count();
	}

	@Override
	@Transactional
	public MilestoneEntity createMilestone(MilestoneEntity milestoneEntity) {
		return milestoneRepository.save(milestoneEntity);
	}

	@Override
	public MilestoneEntity getMilestoneById(Long id) {
		return milestoneRepository.findById(id).orElse(null);
	}

}
