package com.minimarket.security.handler;

import com.minimarket.security.monitor.SuspiciousActivityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private SuspiciousActivityService suspiciousActivityService;

    @InjectMocks
    private JwtAuthenticationEntryPoint entryPoint;

    @Test
    @DisplayName("GIVEN Default Context WHEN Commence THEN Responde401 Con Json")
    void givenDefaultContext_whenCommence_thenResponde401ConJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/productos");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(suspiciousActivityService.clientIp(request)).thenReturn("127.0.0.1");

        entryPoint.commence(request, response, new BadCredentialsException("sin token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString())
                .contains("No autenticado")
                .contains("Se requiere un token JWT válido");
    }
}
