package com.minimarket.security.listener;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.config.LoginAttemptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthenticationFailureListenerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LoginAttemptProperties loginAttemptProperties;

    @InjectMocks
    private AuthenticationFailureListener listener;

    @BeforeEach
    void setUp() {
        when(loginAttemptProperties.getMaxAttempts()).thenReturn(3);
        when(loginAttemptProperties.getLockMinutes()).thenReturn(15);
    }

    @Test
    @DisplayName("GIVEN Username Null WHEN On Application Event THEN No Hace Nada")
    void givenUsernameNull_whenOnApplicationEvent_thenNoHaceNada() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(null);
        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));

        listener.onApplicationEvent(event);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("GIVEN Username Blank WHEN On Application Event THEN No Hace Nada")
    void givenUsernameBlank_whenOnApplicationEvent_thenNoHaceNada() {
        Authentication auth = new UsernamePasswordAuthenticationToken("   ", "wrong");
        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));

        listener.onApplicationEvent(event);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("GIVEN Usuario No Existe WHEN On Application Event THEN No Guarda")
    void givenUsuarioNoExiste_whenOnApplicationEvent_thenNoGuarda() {
        Authentication auth = new UsernamePasswordAuthenticationToken("desconocido", "wrong");
        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        listener.onApplicationEvent(event);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Bajo Max Intentos WHEN On Application Event THEN Incrementa Contador")
    void givenBajoMaxIntentos_whenOnApplicationEvent_thenIncrementaContador() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setFailedLoginAttempts(1);
        Authentication auth = new UsernamePasswordAuthenticationToken("cliente1", "wrong");
        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        listener.onApplicationEvent(event);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(2);
        assertThat(captor.getValue().isAccountLocked()).isFalse();
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("GIVEN Alcanza Max Intentos WHEN On Application Event THEN Bloquea Cuenta")
    void givenAlcanzaMaxIntentos_whenOnApplicationEvent_thenBloqueaCuenta() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setFailedLoginAttempts(2);
        Authentication auth = new UsernamePasswordAuthenticationToken("cliente1", "wrong");
        AuthenticationFailureBadCredentialsEvent event =
                new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        listener.onApplicationEvent(event);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(3);
        assertThat(captor.getValue().isAccountLocked()).isTrue();
        assertThat(captor.getValue().getLockedUntil()).isNotNull();
    }
}
