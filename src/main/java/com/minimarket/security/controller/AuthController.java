package com.minimarket.security.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.model.*;
import com.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.security.service.AuthService;
import com.minimarket.security.service.LoginAttemptService;
import com.minimarket.security.service.MfaService;
import com.minimarket.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDateTime;

@Tag(name = "Autenticación", description = "Login, registro, refresh token, logout y MFA")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SuspiciousActivityService suspiciousActivityService;

    @Autowired
    private Clock clock;

    @Operation(summary = "Iniciar sesión",
            description = "Retorna accessToken y refreshToken. Usuarios demo: cliente1/cliente123, empleado1/empleado123, gerente1/gerente123")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso o desafío MFA"),
            @ApiResponse(responseCode = "400", description = "Credenciales nulas o vacías", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
            @ApiResponse(responseCode = "429", description = "Cuenta bloqueada por intentos fallidos", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = """
                            {"username":"cliente1","password":"cliente123"}
                            """)))
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        loginAttemptService.checkNotBlocked(request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
            usuario.setLastLoginAt(LocalDateTime.now(clock));
            usuarioRepository.save(usuario);

            if (mfaService.isGerenteWithMfa(usuario)) {
                String mfaToken = jwtUtil.generateMfaToken(usuario.getUsername());
                return ResponseEntity.ok(TokenPairResponse.mfaChallenge(mfaToken));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            TokenPairResponse response = authService.issueTokenPair(userDetails);
            log.info("Login exitoso user={} ip={}", request.getUsername(),
                    suspiciousActivityService.clientIp(httpRequest));
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            suspiciousActivityService.recordFailedLogin(httpRequest, request.getUsername());
            throw ex;
        }
    }

    @Operation(summary = "Registrar nuevo usuario cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado con tokens"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@Valid @RequestBody RegisterRequest request) {
        TokenPairResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Renovar access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nuevo par de tokens"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        try {
            TokenPairResponse response = authService.refreshAccessToken(request.getRefreshToken());
            log.info("Refresh token exitoso user={} ip={}", response.getUsername(),
                    suspiciousActivityService.clientIp(httpRequest));
            return ResponseEntity.ok(response);
        } catch (com.minimarket.security.exception.InvalidRefreshTokenException ex) {
            suspiciousActivityService.recordInvalidJwt(httpRequest, ex);
            throw ex;
        }
    }

    @Operation(summary = "Cerrar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<MapMessage> logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {
        authService.logout(request.getRefreshToken());
        log.info("Logout exitoso ip={}", suspiciousActivityService.clientIp(httpRequest));
        return ResponseEntity.ok(new MapMessage("Sesión cerrada correctamente"));
    }

    @Operation(summary = "Configurar MFA TOTP", description = "Solo GERENTE autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR y secreto para configurar MFA"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping("/mfa/setup")
    @PreAuthorize("hasRole('" + SecurityRoles.GERENTE + "')")
    public ResponseEntity<MfaSetupResponse> setupMfa() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(mfaService.setupMfa(username));
    }

    @Operation(summary = "Confirmar activación MFA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MFA activado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping("/mfa/confirm")
    @PreAuthorize("hasRole('" + SecurityRoles.GERENTE + "')")
    public ResponseEntity<MapMessage> confirmMfa(@Valid @RequestBody MfaConfirmRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        mfaService.confirmMfa(username, request.getCode());
        return ResponseEntity.ok(new MapMessage("MFA activado correctamente"));
    }

    @Operation(summary = "Verificar código MFA y obtener tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos tras verificación MFA"),
            @ApiResponse(responseCode = "401", description = "Token MFA o código TOTP inválido", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/mfa/verify")
    public ResponseEntity<TokenPairResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        if (!jwtUtil.isMfaToken(request.getMfaToken())) {
            throw new BadCredentialsException("Token MFA inválido");
        }
        String username = jwtUtil.extractUsername(request.getMfaToken());
        if (!jwtUtil.validateToken(request.getMfaToken(), username)) {
            throw new BadCredentialsException("Token MFA expirado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!mfaService.verifyCode(usuario, request.getCode())) {
            throw new BadCredentialsException("Código TOTP inválido");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(usuario.getRoles().stream()
                        .map(r -> SecurityRoles.toAuthority(r.getNombre()))
                        .toArray(String[]::new))
                .build();

        return ResponseEntity.ok(authService.issueTokenPair(userDetails));
    }

    public record MapMessage(String message) {
    }
}
