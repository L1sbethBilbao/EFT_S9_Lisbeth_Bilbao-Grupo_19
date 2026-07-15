package com.minimarket.security.filter;

import com.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private SuspiciousActivityService suspiciousActivityService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Sin Authorization Header THEN Continua Cadena")
    void givenDefaultContext_whenSinAuthorizationHeader_thenContinuaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(suspiciousActivityService).recordRequest(request);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Authorization Mal Formado THEN Continua Cadena")
    void givenDefaultContext_whenAuthorizationMalFormado_thenContinuaCadena() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");
        when(request.getRequestURI()).thenReturn("/api/usuarios");
        when(suspiciousActivityService.clientIp(request)).thenReturn("127.0.0.1");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Jwt Invalido THEN Registra Actividad Sospechosa")
    void givenDefaultContext_whenJwtInvalido_thenRegistraActividadSospechosa() throws Exception {
        JwtException ex = new JwtException("token inválido");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(request.getRequestURI()).thenReturn("/api/usuarios");
        when(suspiciousActivityService.clientIp(request)).thenReturn("127.0.0.1");
        when(jwtUtil.extractUsername("bad-token")).thenThrow(ex);

        filter.doFilter(request, response, filterChain);

        verify(suspiciousActivityService).recordInvalidJwt(request, ex);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Token No Es Access THEN Registra Actividad Sospechosa")
    void givenDefaultContext_whenTokenNoEsAccess_thenRegistraActividadSospechosa() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer refresh-token");
        when(jwtUtil.extractUsername("refresh-token")).thenReturn("cliente1");
        when(jwtUtil.isAccessToken("refresh-token")).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/api/productos");
        when(suspiciousActivityService.clientIp(request)).thenReturn("10.0.0.1");

        filter.doFilter(request, response, filterChain);

        verify(suspiciousActivityService).recordInvalidJwt(request, null);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Token Access Invalido THEN No Autentica")
    void givenDefaultContext_whenTokenAccessInvalido_thenNoAutentica() throws Exception {
        UserDetails userDetails = User.builder()
                .username("cliente1")
                .password("hash")
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
        when(jwtUtil.extractUsername("access-token")).thenReturn("cliente1");
        when(jwtUtil.isAccessToken("access-token")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("cliente1")).thenReturn(userDetails);
        when(jwtUtil.validateAccessToken("access-token", "cliente1")).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/api/productos");

        filter.doFilter(request, response, filterChain);

        verify(suspiciousActivityService).recordInvalidJwt(request, null);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Token Access Valido THEN Autentica Usuario")
    void givenDefaultContext_whenTokenAccessValido_thenAutenticaUsuario() throws Exception {
        UserDetails userDetails = User.builder()
                .username("cliente1")
                .password("hash")
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.extractUsername("valid-token")).thenReturn("cliente1");
        when(jwtUtil.isAccessToken("valid-token")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("cliente1")).thenReturn(userDetails);
        when(jwtUtil.validateAccessToken("valid-token", "cliente1")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Ya Autenticado THEN No Sobrescribe Contexto")
    void givenDefaultContext_whenYaAutenticado_thenNoSobrescribeContexto() throws Exception {
        UsernamePasswordAuthenticationToken existing = new UsernamePasswordAuthenticationToken(
                "existente", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(existing);

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn("cliente1");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain).doFilter(request, response);
    }
}
