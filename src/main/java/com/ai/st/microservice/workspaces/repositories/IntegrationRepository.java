package com.ai.st.microservice.workspaces.repositories;

import org.springframework.data.repository.CrudRepository;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;

public interface IntegrationRepository extends CrudRepository<IntegrationEntity, Long> {

	IntegrationEntity findByPendingAndMunicipality(Boolean pending, MunicipalityEntity municipality);

}
