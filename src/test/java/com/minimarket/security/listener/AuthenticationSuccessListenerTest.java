package com.minimarket.security.listener;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationSuccessListenerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthenticationSuccessListener listener;

    @Test
    @DisplayName("GIVEN Username Null WHEN On Application Event THEN No Hace Nada")
    void givenUsernameNull_whenOnApplicationEvent_thenNoHaceNada() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(null);
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);

        listener.onApplicationEvent(event);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("GIVEN Username Blank WHEN On Application Event THEN No Hace Nada")
    void givenUsernameBlank_whenOnApplicationEvent_thenNoHaceNada() {
        Authentication auth = new UsernamePasswordAuthenticationToken("  ", "pass");
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);

        listener.onApplicationEvent(event);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("GIVEN Usuario No Existe WHEN On Application Event THEN No Guarda")
    void givenUsuarioNoExiste_whenOnApplicationEvent_thenNoGuarda() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "desconocido", "pass", List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        listener.onApplicationEvent(event);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Usuario Existe WHEN On Application Event THEN Reinicia Intentos Y Desbloquea")
    void givenUsuarioExiste_whenOnApplicationEvent_thenReiniciaIntentosYDesbloquea() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente1");
        usuario.setFailedLoginAttempts(5);
        usuario.setAccountLocked(true);
        usuario.setLockedUntil(LocalDateTime.now().plusMinutes(10));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "cliente1", "pass", List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuario));

        listener.onApplicationEvent(event);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
        assertThat(captor.getValue().isAccountLocked()).isFalse();
        assertThat(captor.getValue().getLockedUntil()).isNull();
    }
}
