package com.minimarket.security.service;

import com.minimarket.entity.RefreshToken;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RefreshTokenRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.exception.InvalidRefreshTokenException;
import com.minimarket.security.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UsuarioRepository usuarioRepository,
            JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public RefreshToken createRefreshToken(String username, String tokenValue) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidRefreshTokenException("Usuario no encontrado"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpiration() / 1000));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String tokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token no registrado"));

        if (stored.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token revocado");
        }
        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }
        if (!jwtUtil.isRefreshToken(tokenValue)) {
            throw new InvalidRefreshTokenException("Token no es de tipo refresh");
        }
        if (!jwtUtil.validateToken(tokenValue, stored.getUsuario().getUsername())) {
            throw new InvalidRefreshTokenException("Refresh token inválido");
        }
        return stored;
    }

    @Transactional
    public void revokeToken(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAndReplace(String oldTokenValue, String newTokenValue, String username) {
        revokeToken(oldTokenValue);
        createRefreshToken(username, newTokenValue);
    }
}
