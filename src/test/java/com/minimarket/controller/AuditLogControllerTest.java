package com.minimarket.controller;

import com.minimarket.entity.AuditLog;
import com.minimarket.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogController auditLogController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Audit Logs THEN Retorna Logs Ordenados")
    void givenDefaultContext_whenListarAuditLogs_thenRetornaLogsOrdenados() {
        List<AuditLog> logs = List.of(new AuditLog());
        when(auditLogRepository.findByResourceOrderByTimestampDesc("/api/usuarios")).thenReturn(logs);

        assertThat(auditLogController.listarAuditLogs()).isSameAs(logs);
    }
}
