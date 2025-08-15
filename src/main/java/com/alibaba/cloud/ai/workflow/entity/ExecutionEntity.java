package com.alibaba.cloud.ai.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "executions")
@Data
public class ExecutionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String workflowId;
    
    @Column(columnDefinition = "TEXT")
    private String inputs;
    
    @Column(columnDefinition = "TEXT")
    private String outputs;
    
    @Column(nullable = false)
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Long duration;
}