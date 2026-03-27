package com.jtcool.common.workflow.repository;

import com.jtcool.common.workflow.domain.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {

    List<WorkflowTransition> findByWorkflowIdAndFromStateIdOrderByPriorityAsc(Long workflowId, Long fromStateId);

    List<WorkflowTransition> findByWorkflowId(Long workflowId);
}
