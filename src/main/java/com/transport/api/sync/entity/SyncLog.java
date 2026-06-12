package com.transport.api.sync.entity;

import com.transport.api.sync.enums.OperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_log")
@Getter
@Setter
public class SyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agence_id", nullable = false)
    private Long agenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @Column(name = "payload", nullable = false, columnDefinition = "JSONB")
    private String payload; // Stocké en JSON

    @Column(name = "local_timestamp", nullable = false)
    private LocalDateTime localTimestamp;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "conflict_resolved")
    private Boolean conflictResolved = false;
}