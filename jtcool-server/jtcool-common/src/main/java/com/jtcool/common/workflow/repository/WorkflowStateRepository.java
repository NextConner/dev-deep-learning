package com.jtcool.common.workflow.repository;

import com.jtcool.common.workflow.domain.WorkflowState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStateRepository extends JpaRepository<WorkflowState, Long> {

    List<WorkflowState> findByWorkflowId(Long workflowId);

    Optional<WorkflowState> findByWorkflowIdAndStateCode(Long workflowId, String stateCode);
}
