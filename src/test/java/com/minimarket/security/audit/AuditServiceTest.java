package com.minimarket.security.audit;

import com.minimarket.entity.AuditLog;
import com.minimarket.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("GIVEN Con Username Null WHEN Log THEN Usa Anonymous")
    void givenConUsernameNull_whenLog_thenUsaAnonymous() {
        auditService.log(null, AuditAction.LIST, "/api/usuarios", 1L, "127.0.0.1", "JUnit", true);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog entry = captor.getValue();
        assertThat(entry.getUsername()).isEqualTo("anonymous");
        assertThat(entry.getAction()).isEqualTo("LIST");
        assertThat(entry.getResource()).isEqualTo("/api/usuarios");
        assertThat(entry.getResourceId()).isEqualTo(1L);
        assertThat(entry.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(entry.getUserAgent()).isEqualTo("JUnit");
        assertThat(entry.isSuccess()).isTrue();
        assertThat(entry.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("GIVEN Con Datos Completos WHEN Log THEN Guarda Entrada")
    void givenConDatosCompletos_whenLog_thenGuardaEntrada() {
        auditService.log("gerente1", AuditAction.DELETE, "/api/usuarios", 9L, "10.0.0.1", "Postman", false);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog entry = captor.getValue();
        assertThat(entry.getUsername()).isEqualTo("gerente1");
        assertThat(entry.getAction()).isEqualTo("DELETE");
        assertThat(entry.isSuccess()).isFalse();
    }
}
