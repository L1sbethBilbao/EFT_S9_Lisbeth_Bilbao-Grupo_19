package com.minimarket.security.service;

import com.minimarket.entity.RefreshToken;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.exception.InvalidRefreshTokenException;
import com.minimarket.security.model.RegisterRequest;
import com.minimarket.security.model.TokenPairResponse;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private AuthService authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        when(inputSanitizer.sanitize(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        userDetails = User.builder()
                .username("cliente1")
                .password("hash")
                .authorities(List.of(new SimpleGrantedAuthority(SecurityRoles.toAuthority(SecurityRoles.CLIENTE))))
                .build();
    }

    @Test
    @DisplayName("GIVEN Usuario Duplicado WHEN Register THEN Lanza Excepcion")
    void givenUsuarioDuplicado_whenRegister_thenLanzaExcepcion() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("cliente1");
        request.setPassword("test1234");
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setEmail("ana@test.cl");
        request.setDireccion("Calle 1");

        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está registrado");
    }

    @Test
    @DisplayName("GIVEN Rol Cliente No Configurado WHEN Register THEN Lanza Excepcion")
    void givenRolClienteNoConfigurado_whenRegister_thenLanzaExcepcion() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevo1");
        request.setPassword("test1234");
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setEmail("nuevo1@test.cl");
        request.setDireccion("Calle 1");

        when(usuarioRepository.findByUsername("nuevo1")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Rol CLIENTE no configurado");
    }

    @Test
    @DisplayName("GIVEN Usuario Nuevo WHEN Register THEN Emite Par De Tokens")
    void givenUsuarioNuevo_whenRegister_thenEmiteParDeTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevo1");
        request.setPassword("test1234");
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setEmail("nuevo1@test.cl");
        request.setDireccion("Calle 1");

        Rol cliente = new Rol();
        cliente.setNombre(SecurityRoles.CLIENTE);

        when(usuarioRepository.findByUsername("nuevo1")).thenReturn(Optional.empty());
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("test1234")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        UserDetails nuevoUserDetails = User.builder()
                .username("nuevo1")
                .password("hash")
                .authorities(List.of(new SimpleGrantedAuthority(SecurityRoles.toAuthority(SecurityRoles.CLIENTE))))
                .build();
        when(userDetailsService.loadUserByUsername("nuevo1")).thenReturn(nuevoUserDetails);
        when(jwtUtil.generateAccessToken(nuevoUserDetails)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("nuevo1")).thenReturn("refresh-token");
        when(jwtUtil.getAccessExpiration()).thenReturn(900_000L);

        TokenPairResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenService).createRefreshToken(eq("nuevo1"), eq("refresh-token"));
    }

    @Test
    @DisplayName("GIVEN Token Valido WHEN Refresh Access Token THEN Emite Nuevo Par")
    void givenTokenValido_whenRefreshAccessToken_thenEmiteNuevoPar() {
        RefreshToken stored = new RefreshToken();
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        stored.setUsuario(usuario);

        when(refreshTokenService.verifyRefreshToken("old-refresh")).thenReturn(stored);
        when(userDetailsService.loadUserByUsername("cliente1")).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(userDetails)).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken("cliente1")).thenReturn("new-refresh");
        when(jwtUtil.getAccessExpiration()).thenReturn(900_000L);

        TokenPairResponse response = authService.refreshAccessToken("old-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenService).revokeAndReplace("old-refresh", "new-refresh", "cliente1");
    }

    @Test
    @DisplayName("GIVEN Token Valido WHEN Logout THEN Revoca Refresh")
    void givenTokenValido_whenLogout_thenRevocaRefresh() {
        RefreshToken stored = new RefreshToken();
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        stored.setUsuario(usuario);

        when(refreshTokenService.verifyRefreshToken("refresh-1")).thenReturn(stored);

        authService.logout("refresh-1");

        verify(refreshTokenService).revokeToken("refresh-1");
    }

    @Test
    @DisplayName("GIVEN Token Invalido WHEN Logout THEN Propaga Excepcion")
    void givenTokenInvalido_whenLogout_thenPropagaExcepcion() {
        when(refreshTokenService.verifyRefreshToken("bad"))
                .thenThrow(new InvalidRefreshTokenException("inválido"));

        assertThatThrownBy(() -> authService.logout("bad"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenService).revokeToken("bad");
    }
}
