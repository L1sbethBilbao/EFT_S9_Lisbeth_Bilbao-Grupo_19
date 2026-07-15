package com.minimarket.security.service;

import com.minimarket.entity.RefreshToken;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RefreshTokenRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.exception.InvalidRefreshTokenException;
import com.minimarket.security.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("GIVEN Usuario Valido WHEN Create Refresh Token THEN Guarda Token")
    void givenUsuarioValido_whenCreateRefreshToken_thenGuardaToken() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));
        when(jwtUtil.getRefreshExpiration()).thenReturn(604_800_000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken("cliente1", "refresh-value");

        assertThat(created.getToken()).isEqualTo("refresh-value");
        assertThat(created.getUsuario()).isSameAs(usuario);
        assertThat(created.isRevoked()).isFalse();
        assertThat(created.getExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("GIVEN Usuario Inexistente WHEN Create Refresh Token THEN Lanza Excepcion")
    void givenUsuarioInexistente_whenCreateRefreshToken_thenLanzaExcepcion() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.createRefreshToken("desconocido", "token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("GIVEN No Registrado WHEN Verify Refresh Token THEN Lanza Excepcion")
    void givenNoRegistrado_whenVerifyRefreshToken_thenLanzaExcepcion() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("missing"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("no registrado");
    }

    @Test
    @DisplayName("GIVEN Revocado WHEN Verify Refresh Token THEN Lanza Excepcion")
    void givenRevocado_whenVerifyRefreshToken_thenLanzaExcepcion() {
        RefreshToken token = buildToken("abc", false);
        token.setRevoked(true);
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("abc"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("revocado");
    }

    @Test
    @DisplayName("GIVEN Expirado WHEN Verify Refresh Token THEN Lanza Excepcion")
    void givenExpirado_whenVerifyRefreshToken_thenLanzaExcepcion() {
        RefreshToken token = buildToken("abc", false);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("abc"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    @DisplayName("GIVEN No Es Refresh WHEN Verify Refresh Token THEN Lanza Excepcion")
    void givenNoEsRefresh_whenVerifyRefreshToken_thenLanzaExcepcion() {
        RefreshToken token = buildToken("abc", false);
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(jwtUtil.isRefreshToken("abc")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("abc"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("no es de tipo refresh");
    }

    @Test
    @DisplayName("GIVEN Jwt Invalido WHEN Verify Refresh Token THEN Lanza Excepcion")
    void givenJwtInvalido_whenVerifyRefreshToken_thenLanzaExcepcion() {
        RefreshToken token = buildToken("abc", false);
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(jwtUtil.isRefreshToken("abc")).thenReturn(true);
        when(jwtUtil.validateToken("abc", "cliente1")).thenReturn(false);

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("abc"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("GIVEN Valido WHEN Verify Refresh Token THEN Retorna Token")
    void givenValido_whenVerifyRefreshToken_thenRetornaToken() {
        RefreshToken token = buildToken("abc", false);
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(jwtUtil.isRefreshToken("abc")).thenReturn(true);
        when(jwtUtil.validateToken("abc", "cliente1")).thenReturn(true);

        RefreshToken verified = refreshTokenService.verifyRefreshToken("abc");

        assertThat(verified).isSameAs(token);
    }

    @Test
    @DisplayName("GIVEN Existente WHEN Revoke Token THEN Marca Revocado")
    void givenExistente_whenRevokeToken_thenMarcaRevocado() {
        RefreshToken token = buildToken("abc", false);
        when(refreshTokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(token)).thenReturn(token);

        refreshTokenService.revokeToken("abc");

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    @DisplayName("GIVEN Inexistente WHEN Revoke Token THEN No Hace Nada")
    void givenInexistente_whenRevokeToken_thenNoHaceNada() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        refreshTokenService.revokeToken("missing");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Revoke And Replace THEN Revoca Y Crea Nuevo Token")
    void givenDefaultContext_whenRevokeAndReplace_thenRevocaYCreaNuevoToken() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        RefreshToken oldToken = buildToken("old", false);
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));
        when(jwtUtil.getRefreshExpiration()).thenReturn(604_800_000L);

        refreshTokenService.revokeAndReplace("old", "new", "cliente1");

        assertThat(oldToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    private RefreshToken buildToken(String value, boolean revoked) {
        RefreshToken token = new RefreshToken();
        token.setToken(value);
        token.setRevoked(revoked);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        token.setUsuario(usuario);
        return token;
    }
}
