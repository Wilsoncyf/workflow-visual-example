package com.alibaba.cloud.ai.workflow.controller;

import com.alibaba.cloud.ai.workflow.entity.ExecutionEntity;
import com.alibaba.cloud.ai.workflow.entity.WorkflowEntity;
import com.alibaba.cloud.ai.workflow.model.WorkflowDefinition;
import com.alibaba.cloud.ai.workflow.service.ExecutionService;
import com.alibaba.cloud.ai.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class WorkflowController {
    
    private final WorkflowService workflowService;
    private final ExecutionService executionService;
    
    @PostMapping
    public ResponseEntity<WorkflowDefinition> createWorkflow(@RequestBody WorkflowDefinition definition) {
        log.info("Creating workflow: {}", definition.getName());
        WorkflowDefinition created = workflowService.createWorkflow(definition);
        return ResponseEntity.ok(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowDefinition> updateWorkflow(
            @PathVariable String id,
            @RequestBody WorkflowDefinition definition) {
        log.info("Updating workflow: {}", id);
        // Clear cache when workflow is updated
        executionService.clearGraphCache(id);
        WorkflowDefinition updated = workflowService.updateWorkflow(id, definition);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDefinition> getWorkflow(@PathVariable String id) {
        log.info("Getting workflow: {}", id);
        WorkflowDefinition workflow = workflowService.getWorkflow(id);
        return ResponseEntity.ok(workflow);
    }
    
    @GetMapping
    public ResponseEntity<List<WorkflowEntity>> listWorkflows() {
        log.info("Listing all workflows");
        List<WorkflowEntity> workflows = workflowService.listWorkflows();
        return ResponseEntity.ok(workflows);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable String id) {
        log.info("Deleting workflow: {}", id);
        workflowService.deleteWorkflow(id);
        executionService.clearGraphCache(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/execute")
    public ResponseEntity<ExecutionEntity> executeWorkflow(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Object> inputs) {
        log.info("Executing workflow: {} with inputs: {}", id, inputs);
        ExecutionEntity execution = executionService.executeWorkflow(id, inputs);
        return ResponseEntity.ok(execution);
    }
    
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<ExecutionEntity> getExecution(@PathVariable String executionId) {
        log.info("Getting execution: {}", executionId);
        ExecutionEntity execution = executionService.getExecution(executionId);
        return ResponseEntity.ok(execution);
    }
}