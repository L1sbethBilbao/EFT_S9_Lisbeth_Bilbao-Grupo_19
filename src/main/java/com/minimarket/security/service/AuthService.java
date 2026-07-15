package com.minimarket.security.service;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final InputSanitizer inputSanitizer;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService,
            InputSanitizer inputSanitizer) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.inputSanitizer = inputSanitizer;
    }

    @Transactional
    public TokenPairResponse register(RegisterRequest request) {
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }

        Rol rolCliente = rolRepository.findByNombre(SecurityRoles.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("Rol CLIENTE no configurado en el sistema"));

        Usuario usuario = new Usuario();
        usuario.setUsername(inputSanitizer.sanitize(request.getUsername()));
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(inputSanitizer.sanitize(request.getNombre()));
        usuario.setApellido(inputSanitizer.sanitize(request.getApellido()));
        usuario.setEmail(inputSanitizer.sanitize(request.getEmail()));
        usuario.setDireccion(inputSanitizer.sanitize(request.getDireccion()));
        usuario.setRoles(Set.of(rolCliente));

        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        return issueTokenPair(userDetails);
    }

    @Transactional
    public TokenPairResponse issueTokenPair(UserDetails userDetails) {
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());
        refreshTokenService.createRefreshToken(userDetails.getUsername(), refreshToken);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace(SecurityRoles.ROLE_PREFIX, ""))
                .collect(Collectors.toList());

        return TokenPairResponse.accessAndRefresh(
                accessToken,
                refreshToken,
                jwtUtil.getAccessExpiration(),
                userDetails.getUsername(),
                roles);
    }

    @Transactional
    public TokenPairResponse refreshAccessToken(String refreshTokenValue) {
        var stored = refreshTokenService.verifyRefreshToken(refreshTokenValue);
        String username = stored.getUsuario().getUsername();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccess = jwtUtil.generateAccessToken(userDetails);
        String newRefresh = jwtUtil.generateRefreshToken(username);
        refreshTokenService.revokeAndReplace(refreshTokenValue, newRefresh, username);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace(SecurityRoles.ROLE_PREFIX, ""))
                .collect(Collectors.toList());

        return TokenPairResponse.accessAndRefresh(
                newAccess,
                newRefresh,
                jwtUtil.getAccessExpiration(),
                username,
                roles);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        try {
            refreshTokenService.verifyRefreshToken(refreshTokenValue);
            refreshTokenService.revokeToken(refreshTokenValue);
        } catch (InvalidRefreshTokenException ex) {
            refreshTokenService.revokeToken(refreshTokenValue);
            throw ex;
        }
    }
}
