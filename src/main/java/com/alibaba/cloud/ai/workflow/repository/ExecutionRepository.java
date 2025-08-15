package com.alibaba.cloud.ai.workflow.repository;

import com.alibaba.cloud.ai.workflow.entity.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<ExecutionEntity, String> {
    List<ExecutionEntity> findByWorkflowIdOrderByStartedAtDesc(String workflowId);
    List<ExecutionEntity> findByStatus(String status);
}