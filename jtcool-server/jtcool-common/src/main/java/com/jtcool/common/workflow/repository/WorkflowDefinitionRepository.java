package com.jtcool.common.workflow.repository;

import com.jtcool.common.workflow.domain.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    Optional<WorkflowDefinition> findByWorkflowCode(String workflowCode);

    Optional<WorkflowDefinition> findByWorkflowCodeAndIsActive(String workflowCode, Boolean isActive);
}
