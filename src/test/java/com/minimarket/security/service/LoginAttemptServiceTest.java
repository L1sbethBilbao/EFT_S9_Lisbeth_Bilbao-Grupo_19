package com.minimarket.security.service;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.config.LoginAttemptProperties;
import com.minimarket.security.exception.AccountLockedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private LoginAttemptProperties properties;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    @Test
    @DisplayName("GIVEN Usuario Inexistente WHEN Check Not Blocked THEN No Hace Nada")
    void givenUsuarioInexistente_whenCheckNotBlocked_thenNoHaceNada() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        loginAttemptService.checkNotBlocked("desconocido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Cuenta Desbloqueada WHEN Check Not Blocked THEN No Lanza Excepcion")
    void givenCuentaDesbloqueada_whenCheckNotBlocked_thenNoLanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setAccountLocked(false);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        loginAttemptService.checkNotBlocked("cliente1");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Cuenta Bloqueada WHEN Check Not Blocked THEN Lanza Excepcion")
    void givenCuentaBloqueada_whenCheckNotBlocked_thenLanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setAccountLocked(true);
        usuario.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> loginAttemptService.checkNotBlocked("cliente1"))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    @DisplayName("GIVEN Cuenta Bloqueada Sin Fecha WHEN Check Not Blocked THEN Lanza Excepcion")
    void givenCuentaBloqueadaSinFecha_whenCheckNotBlocked_thenLanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setAccountLocked(true);
        usuario.setLockedUntil(null);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> loginAttemptService.checkNotBlocked("cliente1"))
                .isInstanceOf(AccountLockedException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Bloqueo Expirado WHEN Check Not Blocked THEN Desbloquea Cuenta")
    void givenBloqueoExpirado_whenCheckNotBlocked_thenDesbloqueaCuenta() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setAccountLocked(true);
        usuario.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        usuario.setFailedLoginAttempts(5);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        loginAttemptService.checkNotBlocked("cliente1");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().isAccountLocked()).isFalse();
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("GIVEN Usuario Inexistente WHEN Login Failed THEN No Guarda")
    void givenUsuarioInexistente_whenLoginFailed_thenNoGuarda() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        loginAttemptService.loginFailed("desconocido");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Bajo Max Intentos WHEN Login Failed THEN Incrementa Contador")
    void givenBajoMaxIntentos_whenLoginFailed_thenIncrementaContador() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setFailedLoginAttempts(1);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));
        when(properties.getMaxAttempts()).thenReturn(5);

        loginAttemptService.loginFailed("cliente1");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(2);
        assertThat(captor.getValue().isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("GIVEN Alcanza Max Intentos WHEN Login Failed THEN Bloquea Cuenta")
    void givenAlcanzaMaxIntentos_whenLoginFailed_thenBloqueaCuenta() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setFailedLoginAttempts(4);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));
        when(properties.getMaxAttempts()).thenReturn(5);
        when(properties.getLockMinutes()).thenReturn(15);

        loginAttemptService.loginFailed("cliente1");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(5);
        assertThat(captor.getValue().isAccountLocked()).isTrue();
        assertThat(captor.getValue().getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Login Succeeded THEN Reinicia Contadores")
    void givenDefaultContext_whenLoginSucceeded_thenReiniciaContadores() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setFailedLoginAttempts(3);
        usuario.setAccountLocked(true);
        usuario.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        loginAttemptService.loginSucceeded("cliente1");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
        assertThat(captor.getValue().isAccountLocked()).isFalse();
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("GIVEN Usuario Inexistente WHEN Login Succeeded THEN No Guarda")
    void givenUsuarioInexistente_whenLoginSucceeded_thenNoGuarda() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        loginAttemptService.loginSucceeded("desconocido");

        verify(usuarioRepository, never()).save(any());
    }
}
