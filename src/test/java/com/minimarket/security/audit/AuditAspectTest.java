package com.minimarket.security.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private AuditAspect auditAspect;

    private Audited audited;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        Method method = AuditTargets.class.getDeclaredMethod("readUsuario", Long.class);
        audited = method.getAnnotation(Audited.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("GIVEN Exito WHEN Audit THEN Registra Log Exitoso")
    void givenExito_whenAudit_thenRegistraLogExitoso() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "gerente1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_GERENTE"))));

        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getArgs()).thenReturn(new Object[]{5L});

        Object result = auditAspect.audit(joinPoint, audited);

        assertThat(result).isEqualTo("ok");
        verify(auditService).log("gerente1", AuditAction.READ, "/api/usuarios", 5L,
                "127.0.0.1", "JUnit", true);
    }

    @Test
    @DisplayName("GIVEN Error WHEN Audit THEN Registra Log Fallido Y Relanza")
    void givenError_whenAudit_thenRegistraLogFallidoYRelanza() throws Throwable {
        RuntimeException error = new RuntimeException("fallo");
        when(joinPoint.proceed()).thenThrow(error);
        when(joinPoint.getArgs()).thenReturn(new Object[]{7L});

        assertThatThrownBy(() -> auditAspect.audit(joinPoint, audited))
                .isSameAs(error);

        verify(auditService).log("anonymous", AuditAction.READ, "/api/usuarios", 7L,
                null, null, false);
    }

    @Test
    @DisplayName("GIVEN Sin Request Ni Autenticacion WHEN Audit THEN Usa Valores Por Defecto")
    void givenSinRequestNiAutenticacion_whenAudit_thenUsaValoresPorDefecto() throws Throwable {
        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getArgs()).thenReturn(null);

        auditAspect.audit(joinPoint, audited);

        verify(auditService).log("anonymous", AuditAction.READ, "/api/usuarios", null,
                null, null, true);
    }

    @Test
    @DisplayName("GIVEN Autenticacion No Autenticada WHEN Audit THEN Usa Anonymous")
    void givenAutenticacionNoAutenticada_whenAudit_thenUsaAnonymous() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("pendiente", "clave"));

        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getArgs()).thenReturn(new Object[]{3L});

        auditAspect.audit(joinPoint, audited);

        verify(auditService).log("anonymous", AuditAction.READ, "/api/usuarios", 3L,
                null, null, true);
    }

    @Test
    @DisplayName("GIVEN Sin Long En Args WHEN Audit THEN No Asigna Resource Id")
    void givenSinLongEnArgs_whenAudit_thenNoAsignaResourceId() throws Throwable {
        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"texto"});

        auditAspect.audit(joinPoint, audited);

        verify(auditService).log("anonymous", AuditAction.READ, "/api/usuarios", null,
                null, null, true);
    }

    static class AuditTargets {
        @Audited(action = AuditAction.READ, resource = "/api/usuarios")
        void readUsuario(Long id) {
        }
    }
}
