package com.minimarket.security.listener;

import com.minimarket.repository.UsuarioRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UsuarioRepository usuarioRepository;

    public AuthenticationSuccessListener(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        if (username == null || username.isBlank()) {
            return;
        }
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            usuario.setFailedLoginAttempts(0);
            usuario.setAccountLocked(false);
            usuario.setLockedUntil(null);
            usuarioRepository.save(usuario);
        });
    }
}
