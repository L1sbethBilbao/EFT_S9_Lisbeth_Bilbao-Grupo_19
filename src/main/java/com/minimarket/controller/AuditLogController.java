package com.minimarket.controller;

import com.minimarket.entity.AuditLog;
import com.minimarket.repository.AuditLogRepository;
import com.minimarket.security.constants.SecurityExpressions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Auditoría", description = "Consulta de logs de auditoría. Solo GERENTE")
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Operation(summary = "Listar logs de auditoría de usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de registros de auditoría"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @GetMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public List<AuditLog> listarAuditLogs() {
        return auditLogRepository.findByResourceOrderByTimestampDesc("/api/usuarios");
    }
}
