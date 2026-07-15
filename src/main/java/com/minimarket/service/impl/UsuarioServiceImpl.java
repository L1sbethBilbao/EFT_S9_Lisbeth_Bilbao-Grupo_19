package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.UsuarioIncompletoException;
import com.minimarket.mapper.UsuarioMapper;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.service.UsuarioService;
import com.minimarket.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll().stream().peek(this::sanitizeForOutput).toList();
    }

    @Override
    public Page<Usuario> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuario -> {
            sanitizeForOutput(usuario);
            return usuario;
        });
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id).map(u -> {
            sanitizeForOutput(u);
            return u;
        });
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username).map(u -> {
            sanitizeForOutput(u);
            return u;
        });
    }

    @Override
    public Usuario save(Usuario usuario) {
        sanitizeForInput(usuario);
        if (usuario.getId() != null) {
            usuarioRepository.findById(usuario.getId()).ifPresent(existing -> {
                if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                    usuario.setPassword(existing.getPassword());
                }
                usuario.setTotpSecret(existing.getTotpSecret());
                usuario.setMfaEnabled(existing.isMfaEnabled());
                usuario.setMfaEnrolledAt(existing.getMfaEnrolledAt());
                usuario.setLastLoginAt(existing.getLastLoginAt());
                usuario.setAnonymized(existing.isAnonymized());
                usuario.setRetentionExcluded(existing.isRetentionExcluded());
            });
        } else if (usuario.getLastLoginAt() == null) {
            usuario.setLastLoginAt(LocalDateTime.now());
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()
                && !isBcryptHash(usuario.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        Usuario saved = usuarioRepository.save(usuario);
        sanitizeForOutput(saved);
        return saved;
    }

    @Override
    public Usuario saveFromDto(UsuarioRequestDTO dto) {
        Usuario usuario = usuarioMapper.toEntity(dto);
        if (dto.getRoleNames() != null && !dto.getRoleNames().isEmpty()) {
            Set<Rol> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Rol rol = rolRepository.findByNombre(roleName)
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format(BusinessErrorMessages.ROL_NO_ENCONTRADO, roleName)));
                roles.add(rol);
            }
            usuario.setRoles(roles);
        }
        return save(usuario);
    }

    @Override
    public boolean tieneDatosCompletos(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return StringUtils.hasText(usuario.getNombre())
                && StringUtils.hasText(usuario.getApellido())
                && StringUtils.hasText(usuario.getEmail())
                && StringUtils.hasText(usuario.getDireccion());
    }

    @Override
    public void validarDatosCompletos(Usuario usuario) {
        if (!tieneDatosCompletos(usuario)) {
            throw new UsuarioIncompletoException(BusinessErrorMessages.USUARIO_DATOS_INCOMPLETOS);
        }
    }

    @Override
    public boolean puedeRegistrarVenta(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return false;
        }
        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .anyMatch(n -> SecurityRoles.EMPLEADO.equals(n)
                        || SecurityRoles.GERENTE.equals(n));
    }

    @Override
    public void validarPuedeRegistrarVenta(Usuario usuario) {
        if (!puedeRegistrarVenta(usuario)) {
            throw new UsuarioIncompletoException(BusinessErrorMessages.USUARIO_SIN_ROL_VALIDO);
        }
    }

    private void sanitizeForInput(Usuario usuario) {
        if (usuario.getUsername() != null) {
            usuario.setUsername(inputSanitizer.sanitize(usuario.getUsername()));
        }
        if (usuario.getNombre() != null) {
            usuario.setNombre(inputSanitizer.sanitize(usuario.getNombre()));
        }
        if (usuario.getApellido() != null) {
            usuario.setApellido(inputSanitizer.sanitize(usuario.getApellido()));
        }
        if (usuario.getEmail() != null) {
            usuario.setEmail(inputSanitizer.sanitize(usuario.getEmail()));
        }
        if (usuario.getDireccion() != null) {
            usuario.setDireccion(inputSanitizer.sanitize(usuario.getDireccion()));
        }
    }

    private void sanitizeForOutput(Usuario usuario) {
        sanitizeForInput(usuario);
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
