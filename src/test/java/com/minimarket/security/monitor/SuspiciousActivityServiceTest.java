package com.minimarket.security.monitor;

import com.minimarket.security.config.SuspiciousActivityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspiciousActivityServiceTest {

    @Mock
    private HttpServletRequest request;

    private SuspiciousActivityProperties properties;
    private SuspiciousActivityService service;

    @BeforeEach
    void setUp() {
        properties = new SuspiciousActivityProperties();
        properties.setFailedLoginThreshold(2);
        properties.setRequestThreshold(3);
        properties.setWindowMinutes(15);
        service = new SuspiciousActivityService(properties);
    }

    @Test
    @DisplayName("GIVEN Con X Forwarded For WHEN Client Ip THEN Usa Primera Ip")
    void givenConXForwardedFor_whenClientIp_thenUsaPrimeraIp() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        assertThat(service.clientIp(request)).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("GIVEN Sin X Forwarded For WHEN Client Ip THEN Usa Remote Addr")
    void givenSinXForwardedFor_whenClientIp_thenUsaRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        assertThat(service.clientIp(request)).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("GIVEN X Forwarded For Vacio WHEN Client Ip THEN Usa Remote Addr")
    void givenXForwardedForVacio_whenClientIp_thenUsaRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        assertThat(service.clientIp(request)).isEqualTo("127.0.0.1");
    }

    @Test
    @DisplayName("GIVEN Con Username WHEN Record Failed Login THEN Registra Intentos")
    void givenConUsername_whenRecordFailedLogin_thenRegistraIntentos() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        service.recordFailedLogin(request, "cliente1");
        service.recordFailedLogin(request, "cliente1");

        assertThat(failedLoginCount("FAILED_LOGIN:cliente1@10.0.0.1")).isEqualTo(2);
    }

    @Test
    @DisplayName("GIVEN Sin Username WHEN Record Failed Login THEN Usa Ip Como Clave")
    void givenSinUsername_whenRecordFailedLogin_thenUsaIpComoClave() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");

        service.recordFailedLogin(request, null);
        service.recordFailedLogin(request, "   ");

        assertThat(failedLoginCount("FAILED_LOGIN:10.0.0.2")).isEqualTo(2);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Record Failed Login THEN Elimina Registros Antiguos")
    void givenDefaultContext_whenRecordFailedLogin_thenEliminaRegistrosAntiguos() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.3");

        List<Long> stale = new ArrayList<>();
        stale.add(Instant.now().toEpochMilli() - properties.getWindowMs() - 1_000L);
        failedLoginMap().put("FAILED_LOGIN:cliente1@10.0.0.3", stale);

        service.recordFailedLogin(request, "cliente1");

        assertThat(failedLoginCount("FAILED_LOGIN:cliente1@10.0.0.3")).isEqualTo(1);
    }

    @Test
    @DisplayName("GIVEN Sin Excepcion WHEN Record Invalid Jwt THEN Registra Motivo Generico")
    void givenSinExcepcion_whenRecordInvalidJwt_thenRegistraMotivoGenerico() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/productos");

        service.recordInvalidJwt(request, null);
    }

    @Test
    @DisplayName("GIVEN Con Excepcion WHEN Record Invalid Jwt THEN Registra Mensaje")
    void givenConExcepcion_whenRecordInvalidJwt_thenRegistraMensaje() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/productos");

        service.recordInvalidJwt(request, new RuntimeException("token expirado"));
    }

    @Test
    @DisplayName("GIVEN Alcanza Umbral WHEN Record Request THEN Registra Alta Tasa")
    void givenAlcanzaUmbral_whenRecordRequest_thenRegistraAltaTasa() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.4");

        service.recordRequest(request);
        service.recordRequest(request);
        service.recordRequest(request);

        assertThat(requestCount("10.0.0.4")).isEqualTo(3);
    }

    @Test
    @DisplayName("GIVEN Cada50 Peticiones WHEN Record Request THEN Registra Info")
    void givenCada50Peticiones_whenRecordRequest_thenRegistraInfo() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        for (int i = 0; i < 50; i++) {
            service.recordRequest(request);
        }

        assertThat(requestCount("10.0.0.5")).isEqualTo(50);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Record Request THEN Elimina Registros Antiguos")
    void givenDefaultContext_whenRecordRequest_thenEliminaRegistrosAntiguos() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.6");

        List<Long> stale = new ArrayList<>();
        stale.add(Instant.now().toEpochMilli() - properties.getWindowMs() - 1_000L);
        requestMap().put("10.0.0.6", stale);

        service.recordRequest(request);

        assertThat(requestCount("10.0.0.6")).isEqualTo(1);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Record Unauthorized Access THEN Registra Intento")
    void givenDefaultContext_whenRecordUnauthorizedAccess_thenRegistraIntento() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/admin");

        service.recordUnauthorizedAccess(request, "/api/admin");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Record Crud Operation THEN Registra Operacion")
    void givenDefaultContext_whenRecordCrudOperation_thenRegistraOperacion() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/productos/1");

        service.recordCrudOperation(request, "DELETE", "/api/productos/1");
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, List<Long>> failedLoginMap() {
        return (ConcurrentHashMap<String, List<Long>>) ReflectionTestUtils.getField(
                service, "failedLoginTimestamps");
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, List<Long>> requestMap() {
        return (ConcurrentHashMap<String, List<Long>>) ReflectionTestUtils.getField(
                service, "requestTimestampsByIp");
    }

    private int failedLoginCount(String key) {
        List<Long> list = failedLoginMap().get(key);
        return list == null ? 0 : list.size();
    }

    private int requestCount(String ip) {
        List<Long> list = requestMap().get(ip);
        return list == null ? 0 : list.size();
    }
}
