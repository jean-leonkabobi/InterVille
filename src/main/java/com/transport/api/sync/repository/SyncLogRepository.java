package com.transport.api.sync.repository;

import com.transport.api.sync.entity.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    List<SyncLog> findByAgenceId(Long agenceId);

    List<SyncLog> findByAgenceIdAndSyncedAtIsNull(Long agenceId);

    List<SyncLog> findByAgenceIdAndConflictResolvedTrue(Long agenceId);
}