package com.alibaba.cloud.ai.workflow.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowDefinition {
    private String id;
    private String name;
    private String description;
    private List<NodeDefinition> nodes;
    private List<EdgeDefinition> edges;
    private Map<String, Object> metadata;
    
    @Data
    public static class NodeDefinition {
        private String id;
        private String type;
        private String label;
        private Position position;
        private Map<String, Object> data;
        private String sourcePosition;
        private String targetPosition;
        
        // 获取节点的实际类型（从 data.nodeType 或 type）
        public String getNodeType() {
            if (data != null && data.containsKey("nodeType")) {
                return (String) data.get("nodeType");
            }
            return type;
        }
        
        @Data
        public static class Position {
            private double x;
            private double y;
        }
    }
    
    @Data
    public static class EdgeDefinition {
        private String id;
        private String source;
        private String target;
        private String sourceHandle;
        private String targetHandle;
        private String type;
        private Map<String, Object> data;
    }
}