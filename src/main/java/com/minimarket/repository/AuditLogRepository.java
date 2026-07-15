package com.minimarket.repository;

import com.minimarket.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByResourceOrderByTimestampDesc(String resource);
}
