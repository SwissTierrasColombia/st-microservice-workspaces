package com.ai.st.microservice.workspaces.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ai.st.microservice.workspaces.entities.IntegrationEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.WorkspaceEntity;

public interface IntegrationRepository extends CrudRepository<IntegrationEntity, Long> {

	IntegrationEntity findByWorkspaceAndState(WorkspaceEntity workspace, IntegrationStateEntity state);

	@Query("SELECT i FROM IntegrationEntity i WHERE i.state.id IN (:statesId) AND i.workspace.id = :workspaceId")
	List<IntegrationEntity> findIntegrationsByWorkspaceAndStates(@Param("workspaceId") Long workspaceId,
			@Param("statesId") List<Long> statesId);

	List<IntegrationEntity> findByWorkspace(WorkspaceEntity workspace);

	IntegrationEntity findBySupplyCadastreIdAndSupplySnrIdAndState(Long supplyCadastreId, Long supplySnrId,
			IntegrationStateEntity state);

}
