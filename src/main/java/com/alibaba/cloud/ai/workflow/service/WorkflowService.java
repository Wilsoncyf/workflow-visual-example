package com.alibaba.cloud.ai.workflow.service;

import com.alibaba.cloud.ai.workflow.entity.WorkflowEntity;
import com.alibaba.cloud.ai.workflow.model.WorkflowDefinition;
import com.alibaba.cloud.ai.workflow.repository.WorkflowRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public WorkflowDefinition createWorkflow(WorkflowDefinition definition) {
        try {
            WorkflowEntity entity = new WorkflowEntity();
            entity.setName(definition.getName());
            entity.setDescription(definition.getDescription());
            entity.setGraphData(objectMapper.writeValueAsString(definition));
            entity.setStatus("draft");
            
            WorkflowEntity saved = workflowRepository.save(entity);
            definition.setId(saved.getId());
            
            log.info("Created workflow: {}", saved.getId());
            return definition;
        } catch (Exception e) {
            log.error("Failed to create workflow", e);
            throw new RuntimeException("Failed to create workflow", e);
        }
    }
    
    @Transactional
    public WorkflowDefinition updateWorkflow(String id, WorkflowDefinition definition) {
        try {
            WorkflowEntity entity = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
            
            entity.setName(definition.getName());
            entity.setDescription(definition.getDescription());
            entity.setGraphData(objectMapper.writeValueAsString(definition));
            
            workflowRepository.save(entity);
            definition.setId(id);
            
            log.info("Updated workflow: {}", id);
            return definition;
        } catch (Exception e) {
            log.error("Failed to update workflow", e);
            throw new RuntimeException("Failed to update workflow", e);
        }
    }
    
    public WorkflowDefinition getWorkflow(String id) {
        try {
            WorkflowEntity entity = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));
            
            WorkflowDefinition definition = objectMapper.readValue(
                entity.getGraphData(), 
                WorkflowDefinition.class
            );
            definition.setId(entity.getId());
            
            return definition;
        } catch (Exception e) {
            log.error("Failed to get workflow", e);
            throw new RuntimeException("Failed to get workflow", e);
        }
    }
    
    public List<WorkflowEntity> listWorkflows() {
        return workflowRepository.findAll();
    }
    
    @Transactional
    public void deleteWorkflow(String id) {
        workflowRepository.deleteById(id);
        log.info("Deleted workflow: {}", id);
    }
}