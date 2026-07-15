package com.minimarket.security.service;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.config.LoginAttemptProperties;
import com.minimarket.security.exception.AccountLockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private final LoginAttemptProperties properties;
    private final UsuarioRepository usuarioRepository;

    public LoginAttemptService(LoginAttemptProperties properties, UsuarioRepository usuarioRepository) {
        this.properties = properties;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void checkNotBlocked(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        if (usuario == null) {
            return;
        }
        unlockIfExpired(usuario);
        if (usuario.isAccountLocked()) {
            throw new AccountLockedException();
        }
    }

    @Transactional
    public void loginFailed(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            int attempts = usuario.getFailedLoginAttempts() + 1;
            usuario.setFailedLoginAttempts(attempts);
            if (attempts >= properties.getMaxAttempts()) {
                usuario.setAccountLocked(true);
                usuario.setLockedUntil(LocalDateTime.now().plusMinutes(properties.getLockMinutes()));
            }
            usuarioRepository.save(usuario);
        });
    }

    @Transactional
    public void loginSucceeded(String username) {
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            usuario.setFailedLoginAttempts(0);
            usuario.setAccountLocked(false);
            usuario.setLockedUntil(null);
            usuarioRepository.save(usuario);
        });
    }

    private void unlockIfExpired(Usuario usuario) {
        if (usuario.isAccountLocked() && usuario.getLockedUntil() != null
                && LocalDateTime.now().isAfter(usuario.getLockedUntil())) {
            usuario.setAccountLocked(false);
            usuario.setFailedLoginAttempts(0);
            usuario.setLockedUntil(null);
            usuarioRepository.save(usuario);
        }
    }
}
