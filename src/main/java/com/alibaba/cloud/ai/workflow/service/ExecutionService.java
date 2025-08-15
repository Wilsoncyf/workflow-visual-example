package com.alibaba.cloud.ai.workflow.service;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.workflow.entity.ExecutionEntity;
import com.alibaba.cloud.ai.workflow.model.WorkflowDefinition;
import com.alibaba.cloud.ai.workflow.repository.ExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {
    
    private final WorkflowService workflowService;
    private final ExecutionRepository executionRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, CompiledGraph> graphCache = new ConcurrentHashMap<>();
    
    public ExecutionEntity executeWorkflow(String workflowId, Map<String, Object> inputs) {
        ExecutionEntity execution = new ExecutionEntity();
        execution.setWorkflowId(workflowId);
        execution.setStatus("running");
        
        try {
            execution.setInputs(objectMapper.writeValueAsString(inputs));
            execution = executionRepository.save(execution);
            
            // 获取工作流定义
            WorkflowDefinition definition = workflowService.getWorkflow(workflowId);
            
            // 编译图
            CompiledGraph compiledGraph = getOrCompileGraph(workflowId, definition);
            
            // 创建初始状态
            OverAllState initialState = OverAllStateBuilder.builder()
                .withData(inputs != null ? inputs : new HashMap<>())
                .build();
            
            // 执行工作流
            String executionId = execution.getId();
            LocalDateTime startTime = LocalDateTime.now();
            
            // 异步执行工作流
            CompletableFuture.supplyAsync(() -> {
                try {
                    return compiledGraph.invoke(initialState, RunnableConfig.builder().build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenAccept(optionalState -> {
                if (optionalState.isPresent()) {
                    updateExecutionSuccess(executionId, optionalState.get(), startTime);
                } else {
                    updateExecutionError(executionId, new RuntimeException("Execution returned empty result"), startTime);
                }
            }).exceptionally(error -> {
                updateExecutionError(executionId, error, startTime);
                return null;
            });
            
            return execution;
            
        } catch (Exception e) {
            log.error("Failed to execute workflow", e);
            execution.setStatus("failed");
            execution.setErrorMessage(e.getMessage());
            executionRepository.save(execution);
            throw new RuntimeException("Failed to execute workflow", e);
        }
    }
    
    private CompiledGraph getOrCompileGraph(String workflowId, WorkflowDefinition definition) {
        return graphCache.computeIfAbsent(workflowId, id -> {
            try {
                // 使用默认的 KeyStrategy
                StateGraph graph = new StateGraph();
                
                // 添加节点
                for (WorkflowDefinition.NodeDefinition node : definition.getNodes()) {
                    AsyncNodeAction action = createNodeAction(node);
                    graph.addNode(node.getId(), action);
                }
                
                // 添加边
                for (WorkflowDefinition.EdgeDefinition edge : definition.getEdges()) {
                    // 处理特殊的开始和结束节点
                    String source = edge.getSource();
                    String target = edge.getTarget();
                    
                    if ("start".equals(source)) {
                        source = StateGraph.START;
                    }
                    if ("end".equals(target)) {
                        target = StateGraph.END;
                    }
                    
                    graph.addEdge(source, target);
                }
                
                // 如果没有明确的开始边，添加默认的
                boolean hasStartEdge = definition.getEdges().stream()
                    .anyMatch(e -> "start".equals(e.getSource()));
                if (!hasStartEdge && !definition.getNodes().isEmpty()) {
                    graph.addEdge(StateGraph.START, definition.getNodes().get(0).getId());
                }
                
                // 如果没有明确的结束边，添加默认的
                boolean hasEndEdge = definition.getEdges().stream()
                    .anyMatch(e -> "end".equals(e.getTarget()));
                if (!hasEndEdge && !definition.getNodes().isEmpty()) {
                    String lastNodeId = definition.getNodes().get(definition.getNodes().size() - 1).getId();
                    graph.addEdge(lastNodeId, StateGraph.END);
                }
                
                return graph.compile();
            } catch (Exception e) {
                log.error("Failed to compile graph", e);
                throw new RuntimeException("Failed to compile graph", e);
            }
        });
    }
    
    private AsyncNodeAction createNodeAction(WorkflowDefinition.NodeDefinition node) {
        return (state) -> {
            String nodeType = node.getNodeType();
            log.info("Executing node: {} ({})", node.getId(), nodeType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("nodeId", node.getId());
            result.put("nodeType", nodeType);
            result.put("nodeLabel", getNodeLabel(node));
            
            // 根据节点类型执行不同的逻辑
            switch (nodeType) {
                case "input":
                    result.put("message", "Input node executed");
                    break;
                    
                case "process":
                    // 模拟处理逻辑
                    Object input = state.value("input").orElse("default");
                    result.put("processed", "Processed: " + input);
                    result.put("timestamp", System.currentTimeMillis());
                    break;
                    
                case "output":
                    result.put("message", "Output node executed");
                    result.put("finalResult", state.data());
                    break;
                    
                case "delay":
                    // 模拟延迟
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    result.put("message", "Delay completed");
                    break;
                    
                default:
                    result.put("message", "Custom node executed: " + nodeType);
                    if (node.getData() != null) {
                        result.putAll(node.getData());
                    }
                    break;
            }
            
            return CompletableFuture.completedFuture(result);
        };
    }
    
    private String getNodeLabel(WorkflowDefinition.NodeDefinition node) {
        if (node.getData() != null && node.getData().containsKey("label")) {
            return (String) node.getData().get("label");
        }
        return node.getId();
    }
    
    private void updateExecutionSuccess(String executionId, OverAllState finalState, LocalDateTime startTime) {
        try {
            ExecutionEntity execution = executionRepository.findById(executionId).orElse(null);
            if (execution != null) {
                execution.setStatus("completed");
                execution.setOutputs(objectMapper.writeValueAsString(finalState.data()));
                execution.setCompletedAt(LocalDateTime.now());
                execution.setDuration(
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis()
                );
                executionRepository.save(execution);
                log.info("Workflow execution completed: {}", executionId);
            }
        } catch (Exception e) {
            log.error("Failed to update execution success", e);
        }
    }
    
    private void updateExecutionError(String executionId, Throwable error, LocalDateTime startTime) {
        try {
            ExecutionEntity execution = executionRepository.findById(executionId).orElse(null);
            if (execution != null) {
                execution.setStatus("failed");
                execution.setErrorMessage(error.getMessage());
                execution.setCompletedAt(LocalDateTime.now());
                execution.setDuration(
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis()
                );
                executionRepository.save(execution);
                log.error("Workflow execution failed: {}", executionId, error);
            }
        } catch (Exception e) {
            log.error("Failed to update execution error", e);
        }
    }
    
    public ExecutionEntity getExecution(String executionId) {
        return executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));
    }
    
    public void clearGraphCache(String workflowId) {
        graphCache.remove(workflowId);
    }
}