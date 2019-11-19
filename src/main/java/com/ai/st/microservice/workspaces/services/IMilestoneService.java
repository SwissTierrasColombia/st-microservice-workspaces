package com.ai.st.microservice.workspaces.services;

import com.ai.st.microservice.workspaces.entities.MilestoneEntity;

public interface IMilestoneService {

	public Long getCount();

	public MilestoneEntity createMilestone(MilestoneEntity milestoneEntity);

	public MilestoneEntity getMilestoneById(Long id);

}
