package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop500ByOrderByCreatedAtDesc();
}
