package com.alibaba.cloud.ai.workflow.repository;

import com.alibaba.cloud.ai.workflow.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowEntity, String> {
    List<WorkflowEntity> findByStatus(String status);
    List<WorkflowEntity> findByNameContaining(String name);
}