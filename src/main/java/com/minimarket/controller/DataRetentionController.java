package com.minimarket.controller;

import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.security.retention.DataRetentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Administración", description = "Tareas administrativas del sistema")
@RestController
@RequestMapping("/api/admin/retention")
public class DataRetentionController {

    private final DataRetentionService dataRetentionService;

    public DataRetentionController(DataRetentionService dataRetentionService) {
        this.dataRetentionService = dataRetentionService;
    }

    @Operation(summary = "Ejecutar retención de datos",
            description = "Anonimiza usuarios inactivos según política configurada. Solo GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proceso ejecutado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping("/run")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Map<String, Object>> runRetention() {
        int count = dataRetentionService.anonymizeInactiveUsers();
        return ResponseEntity.ok(Map.of(
                "message", "Proceso de retención ejecutado",
                "anonymizedCount", count));
    }
}
