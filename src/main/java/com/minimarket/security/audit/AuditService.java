package com.minimarket.security.audit;

import com.minimarket.entity.AuditLog;
import com.minimarket.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username, AuditAction action, String resource, Long resourceId,
                    String ipAddress, String userAgent, boolean success) {
        AuditLog entry = new AuditLog();
        entry.setUsername(username != null ? username : "anonymous");
        entry.setAction(action.name());
        entry.setResource(resource);
        entry.setResourceId(resourceId);
        entry.setIpAddress(ipAddress);
        entry.setUserAgent(userAgent);
        entry.setTimestamp(LocalDateTime.now());
        entry.setSuccess(success);
        auditLogRepository.save(entry);
    }
}
