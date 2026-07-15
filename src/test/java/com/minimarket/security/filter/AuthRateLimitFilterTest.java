package com.minimarket.security.filter;

import com.minimarket.security.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setAuthPerMinute(2);
        filter = new AuthRateLimitFilter(properties);
    }

    @Test
    @DisplayName("GIVEN Ruta No Auth WHEN Should Not Filter THEN Retorna True")
    void givenRutaNoAuth_whenShouldNotFilter_thenRetornaTrue() {
        when(request.getRequestURI()).thenReturn("/api/productos");

        boolean skip = (boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", request);

        assertThat(skip).isTrue();
    }

    @Test
    @DisplayName("GIVEN Ruta Auth WHEN Should Not Filter THEN Retorna False")
    void givenRutaAuth_whenShouldNotFilter_thenRetornaFalse() {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        boolean skip = (boolean) ReflectionTestUtils.invokeMethod(filter, "shouldNotFilter", request);

        assertThat(skip).isFalse();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Bajo Limite THEN Continua Cadena")
    void givenDefaultContext_whenBajoLimite_thenContinuaCadena() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Excede Limite THEN Retorna429")
    void givenDefaultContext_whenExcedeLimite_thenRetorna429() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);
        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(2)).doFilter(request, response);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("Retry-After", "60");
        verify(response).setContentType("application/json");
        assertThat(body.toString()).contains("Demasiadas solicitudes");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Usa X Forwarded For THEN Como Clave Cliente")
    void givenDefaultContext_whenUsaXForwardedFor_thenComoClaveCliente() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, never()).getRemoteAddr();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN X Forwarded For Vacio THEN Usa Remote Addr")
    void givenDefaultContext_whenXForwardedForVacio_thenUsaRemoteAddr() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request).getRemoteAddr();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Ruta No Auth THEN No Aplica Rate Limit")
    void givenDefaultContext_whenRutaNoAuth_thenNoAplicaRateLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/usuarios");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
