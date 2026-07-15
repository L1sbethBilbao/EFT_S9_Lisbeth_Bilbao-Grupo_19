package com.minimarket.security.handler;

import com.minimarket.security.monitor.SuspiciousActivityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAccessDeniedHandlerTest {

    @Mock
    private SuspiciousActivityService suspiciousActivityService;

    @InjectMocks
    private JwtAccessDeniedHandler handler;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GIVEN Usuario Autenticado WHEN Handle THEN Responde403 Con Json")
    void givenUsuarioAutenticado_whenHandle_thenResponde403ConJson() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "gerente1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_GERENTE"))));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(suspiciousActivityService.clientIp(request)).thenReturn("127.0.0.1");

        handler.handle(request, response, new AccessDeniedException("denegado"));

        verify(suspiciousActivityService).recordUnauthorizedAccess(request, "/api/admin");
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .contains("Acceso denegado")
                .contains("No tiene permisos para acceder a este recurso");
    }

    @Test
    @DisplayName("GIVEN Sin Autenticacion WHEN Handle THEN Responde403 Con Json")
    void givenSinAutenticacion_whenHandle_thenResponde403ConJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/usuarios/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(suspiciousActivityService.clientIp(request)).thenReturn("10.0.0.1");

        handler.handle(request, response, new AccessDeniedException("denegado"));

        verify(suspiciousActivityService).recordUnauthorizedAccess(request, "/api/usuarios/1");
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("Acceso denegado");
    }
}
