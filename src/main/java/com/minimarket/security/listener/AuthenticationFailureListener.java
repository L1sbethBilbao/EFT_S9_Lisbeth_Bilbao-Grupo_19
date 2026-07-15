package com.minimarket.security.listener;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.config.LoginAttemptProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptProperties loginAttemptProperties;

    public AuthenticationFailureListener(
            UsuarioRepository usuarioRepository,
            LoginAttemptProperties loginAttemptProperties) {
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptProperties = loginAttemptProperties;
    }

    @Override
    @Transactional
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        if (username == null || username.isBlank()) {
            return;
        }
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            int attempts = usuario.getFailedLoginAttempts() + 1;
            usuario.setFailedLoginAttempts(attempts);
            if (attempts >= loginAttemptProperties.getMaxAttempts()) {
                usuario.setAccountLocked(true);
                usuario.setLockedUntil(LocalDateTime.now()
                        .plusMinutes(loginAttemptProperties.getLockMinutes()));
            }
            usuarioRepository.save(usuario);
        });
    }
}
