package com.minimarket.security.controller;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.exception.AccountLockedException;
import com.minimarket.security.exception.InvalidRefreshTokenException;
import com.minimarket.security.model.*;
import com.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.security.service.AuthService;
import com.minimarket.security.service.LoginAttemptService;
import com.minimarket.security.service.MfaService;
import com.minimarket.security.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthService authService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private MfaService mfaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SuspiciousActivityService suspiciousActivityService;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private Clock clock;

    @InjectMocks
    private AuthController authController;

    private static final Instant FIXED_INSTANT = Instant.parse("2024-06-15T10:00:00Z");
    private static final ZoneId FIXED_ZONE = ZoneId.of("UTC");

    private LoginRequest loginRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("cliente1");
        loginRequest.setPassword("cliente123");

        usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setPassword("hash");
        Rol rol = new Rol();
        rol.setNombre(SecurityRoles.CLIENTE);
        usuario.setRoles(Set.of(rol));
        lenient().when(clock.instant()).thenReturn(FIXED_INSTANT);
        lenient().when(clock.getZone()).thenReturn(FIXED_ZONE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GIVEN Exitoso WHEN Login THEN Emite Token Pair")
    void givenExitoso_whenLogin_thenEmiteTokenPair() {
        UserDetails userDetails = User.builder()
                .username("cliente1")
                .password("hash")
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                .build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        TokenPairResponse tokenPair = TokenPairResponse.accessAndRefresh(
                "access", "refresh", 900_000L, "cliente1", List.of("CLIENTE"));

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));
        when(mfaService.isGerenteWithMfa(usuario)).thenReturn(false);
        when(authService.issueTokenPair(userDetails)).thenReturn(tokenPair);
        when(suspiciousActivityService.clientIp(httpRequest)).thenReturn("127.0.0.1");

        ResponseEntity<TokenPairResponse> response = authController.login(loginRequest, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenPair);
        verify(loginAttemptService).checkNotBlocked("cliente1");
        verify(usuarioRepository).save(usuario);
        assertThat(usuario.getLastLoginAt())
                .isEqualTo(LocalDateTime.ofInstant(FIXED_INSTANT, FIXED_ZONE));
    }

    @Test
    @DisplayName("GIVEN Cuenta Bloqueada WHEN Login THEN Lanza AccountLockedException")
    void givenCuentaBloqueada_whenLogin_thenLanzaAccountLockedException() {
        doThrow(new AccountLockedException())
                .when(loginAttemptService).checkNotBlocked("cliente1");

        assertThatThrownBy(() -> authController.login(loginRequest, httpRequest))
                .isInstanceOf(AccountLockedException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("GIVEN Refresh Token Expirado WHEN Refresh THEN Lanza InvalidRefreshTokenException")
    void givenRefreshTokenExpirado_whenRefresh_thenLanzaInvalidRefreshTokenException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-refresh");
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("Refresh token expirado");

        when(authService.refreshAccessToken("expired-refresh")).thenThrow(ex);

        assertThatThrownBy(() -> authController.refresh(request, httpRequest))
                .isSameAs(ex);

        verify(suspiciousActivityService).recordInvalidJwt(httpRequest, ex);
    }

    @Test
    @DisplayName("GIVEN Gerente Con Mfa WHEN Login THEN Retorna Desafio")
    void givenGerenteConMfa_whenLogin_thenRetornaDesafio() {
        UserDetails userDetails = User.builder()
                .username("gerente1")
                .password("hash")
                .authorities(new SimpleGrantedAuthority("ROLE_GERENTE"))
                .build();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        usuario.setUsername("gerente1");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(Optional.of(usuario));
        when(mfaService.isGerenteWithMfa(usuario)).thenReturn(true);
        when(jwtUtil.generateMfaToken("gerente1")).thenReturn("mfa-token");

        loginRequest.setUsername("gerente1");
        ResponseEntity<TokenPairResponse> response = authController.login(loginRequest, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isMfaRequired()).isTrue();
        assertThat(response.getBody().getMfaToken()).isEqualTo("mfa-token");
        verify(authService, never()).issueTokenPair(any());
    }

    @Test
    @DisplayName("GIVEN Credenciales Invalidas WHEN Login THEN Registra Fallo")
    void givenCredencialesInvalidas_whenLogin_thenRegistraFallo() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authController.login(loginRequest, httpRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(suspiciousActivityService).recordFailedLogin(httpRequest, "cliente1");
    }

    @Test
    @DisplayName("GIVEN Usuario No Encontrado WHEN Login THEN Registra Fallo")
    void givenUsuarioNoEncontrado_whenLogin_thenRegistraFallo() {
        UserDetails userDetails = User.builder()
                .username("cliente1")
                .password("hash")
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                .build();
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(loginRequest, httpRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");

        verify(suspiciousActivityService).recordFailedLogin(httpRequest, "cliente1");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Register THEN Retorna201")
    void givenDefaultContext_whenRegister_thenRetorna201() {
        RegisterRequest request = new RegisterRequest();
        TokenPairResponse tokenPair = TokenPairResponse.accessAndRefresh(
                "access", "refresh", 900_000L, "nuevo", List.of("CLIENTE"));
        when(authService.register(request)).thenReturn(tokenPair);

        ResponseEntity<TokenPairResponse> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(tokenPair);
    }

    @Test
    @DisplayName("GIVEN Exitoso WHEN Refresh THEN Retorna Token Pair")
    void givenExitoso_whenRefresh_thenRetornaTokenPair() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");
        TokenPairResponse tokenPair = TokenPairResponse.accessAndRefresh(
                "new-access", "new-refresh", 900_000L, "cliente1", List.of("CLIENTE"));

        when(authService.refreshAccessToken("refresh-token")).thenReturn(tokenPair);
        when(suspiciousActivityService.clientIp(httpRequest)).thenReturn("10.0.0.1");

        ResponseEntity<TokenPairResponse> response = authController.refresh(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenPair);
    }

    @Test
    @DisplayName("GIVEN Token Invalido WHEN Refresh THEN Registra Actividad Sospechosa")
    void givenTokenInvalido_whenRefresh_thenRegistraActividadSospechosa() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad-token");
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("Token inválido");

        when(authService.refreshAccessToken("bad-token")).thenThrow(ex);

        assertThatThrownBy(() -> authController.refresh(request, httpRequest))
                .isSameAs(ex);

        verify(suspiciousActivityService).recordInvalidJwt(httpRequest, ex);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Logout THEN Retorna Mensaje")
    void givenDefaultContext_whenLogout_thenRetornaMensaje() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");
        when(suspiciousActivityService.clientIp(httpRequest)).thenReturn("127.0.0.1");

        ResponseEntity<AuthController.MapMessage> response = authController.logout(request, httpRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Sesión cerrada correctamente");
        verify(authService).logout("refresh-token");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Setup Mfa THEN Retorna Configuracion")
    void givenDefaultContext_whenSetupMfa_thenRetornaConfiguracion() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "gerente1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_GERENTE"))));
        MfaSetupResponse setup = new MfaSetupResponse("SECRET", "otpauth://...");
        when(mfaService.setupMfa("gerente1")).thenReturn(setup);

        ResponseEntity<MfaSetupResponse> response = authController.setupMfa();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(setup);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Confirm Mfa THEN Retorna Mensaje")
    void givenDefaultContext_whenConfirmMfa_thenRetornaMensaje() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "gerente1", "n/a", List.of(new SimpleGrantedAuthority("ROLE_GERENTE"))));
        MfaConfirmRequest request = new MfaConfirmRequest();
        request.setCode("123456");

        ResponseEntity<AuthController.MapMessage> response = authController.confirmMfa(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("MFA activado correctamente");
        verify(mfaService).confirmMfa("gerente1", "123456");
    }

    @Test
    @DisplayName("GIVEN Token No Es Mfa WHEN Verify Mfa THEN Lanza Excepcion")
    void givenTokenNoEsMfa_whenVerifyMfa_thenLanzaExcepcion() {
        MfaVerifyRequest request = new MfaVerifyRequest();
        request.setMfaToken("bad");
        request.setCode("123456");
        when(jwtUtil.isMfaToken("bad")).thenReturn(false);

        assertThatThrownBy(() -> authController.verifyMfa(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Token MFA inválido");
    }

    @Test
    @DisplayName("GIVEN Token Expirado WHEN Verify Mfa THEN Lanza Excepcion")
    void givenTokenExpirado_whenVerifyMfa_thenLanzaExcepcion() {
        MfaVerifyRequest request = new MfaVerifyRequest();
        request.setMfaToken("mfa-token");
        request.setCode("123456");
        when(jwtUtil.isMfaToken("mfa-token")).thenReturn(true);
        when(jwtUtil.extractUsername("mfa-token")).thenReturn("gerente1");
        when(jwtUtil.validateToken("mfa-token", "gerente1")).thenReturn(false);

        assertThatThrownBy(() -> authController.verifyMfa(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Token MFA expirado");
    }

    @Test
    @DisplayName("GIVEN Usuario No Encontrado WHEN Verify Mfa THEN Lanza Excepcion")
    void givenUsuarioNoEncontrado_whenVerifyMfa_thenLanzaExcepcion() {
        MfaVerifyRequest request = new MfaVerifyRequest();
        request.setMfaToken("mfa-token");
        request.setCode("123456");
        when(jwtUtil.isMfaToken("mfa-token")).thenReturn(true);
        when(jwtUtil.extractUsername("mfa-token")).thenReturn("gerente1");
        when(jwtUtil.validateToken("mfa-token", "gerente1")).thenReturn(true);
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.verifyMfa(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    @DisplayName("GIVEN Codigo Invalido WHEN Verify Mfa THEN Lanza Excepcion")
    void givenCodigoInvalido_whenVerifyMfa_thenLanzaExcepcion() {
        MfaVerifyRequest request = new MfaVerifyRequest();
        request.setMfaToken("mfa-token");
        request.setCode("000000");
        usuario.setUsername("gerente1");
        Rol rol = new Rol();
        rol.setNombre(SecurityRoles.GERENTE);
        usuario.setRoles(Set.of(rol));

        when(jwtUtil.isMfaToken("mfa-token")).thenReturn(true);
        when(jwtUtil.extractUsername("mfa-token")).thenReturn("gerente1");
        when(jwtUtil.validateToken("mfa-token", "gerente1")).thenReturn(true);
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(Optional.of(usuario));
        when(mfaService.verifyCode(usuario, "000000")).thenReturn(false);

        assertThatThrownBy(() -> authController.verifyMfa(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Código TOTP inválido");
    }

    @Test
    @DisplayName("GIVEN Exitoso WHEN Verify Mfa THEN Emite Token Pair")
    void givenExitoso_whenVerifyMfa_thenEmiteTokenPair() {
        MfaVerifyRequest request = new MfaVerifyRequest();
        request.setMfaToken("mfa-token");
        request.setCode("123456");
        usuario.setUsername("gerente1");
        Rol rol = new Rol();
        rol.setNombre(SecurityRoles.GERENTE);
        usuario.setRoles(Set.of(rol));
        TokenPairResponse tokenPair = TokenPairResponse.accessAndRefresh(
                "access", "refresh", 900_000L, "gerente1", List.of("GERENTE"));

        when(jwtUtil.isMfaToken("mfa-token")).thenReturn(true);
        when(jwtUtil.extractUsername("mfa-token")).thenReturn("gerente1");
        when(jwtUtil.validateToken("mfa-token", "gerente1")).thenReturn(true);
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(Optional.of(usuario));
        when(mfaService.verifyCode(usuario, "123456")).thenReturn(true);
        when(authService.issueTokenPair(any(UserDetails.class))).thenReturn(tokenPair);

        ResponseEntity<TokenPairResponse> response = authController.verifyMfa(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenPair);
        verify(authService).issueTokenPair(any(UserDetails.class));
    }
}
