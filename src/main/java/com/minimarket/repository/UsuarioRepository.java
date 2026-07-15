package com.minimarket.repository;

import com.minimarket.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    List<Usuario> findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(LocalDateTime threshold);
}
