package com.jtcool.common.workflow.repository;

import com.jtcool.common.workflow.domain.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {

    List<WorkflowHistory> findByInstanceIdOrderByTransitionTimeDesc(Long instanceId);
}
