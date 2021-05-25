package com.ai.st.microservice.workspaces.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.entities.WorkspaceOperatorEntity;
import com.ai.st.microservice.workspaces.repositories.WorkspaceOperatorRepository;

import java.util.List;

@Service
public class WorkspaceOperatorService implements IWorkspaceOperatorService {

    @Autowired
    private WorkspaceOperatorRepository workspaceOperatorRepository;

    @Override
    @Transactional
    public void deleteWorkspaceOperatorById(Long id) {
        workspaceOperatorRepository.deleteById(id);
    }

    @Override
    @Transactional
    public WorkspaceOperatorEntity createOperator(WorkspaceOperatorEntity operator) {
        return workspaceOperatorRepository.save(operator);
    }

    @Override
    public WorkspaceOperatorEntity getWorkspaceOperatorById(Long id) {
        return workspaceOperatorRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public WorkspaceOperatorEntity updateOperator(WorkspaceOperatorEntity operator) {
        return workspaceOperatorRepository.save(operator);
    }

    @Override
    public List<WorkspaceOperatorEntity> getWorkspacesOperatorsByOperatorCode(Long operatorCode) {
        return workspaceOperatorRepository.findByOperatorCode(operatorCode);
    }

    @Override
    public List<WorkspaceOperatorEntity> getWorkspacesOperatorsByManagerCode(Long managerCode) {
        return workspaceOperatorRepository.findByManagerCode(managerCode);
    }

}
