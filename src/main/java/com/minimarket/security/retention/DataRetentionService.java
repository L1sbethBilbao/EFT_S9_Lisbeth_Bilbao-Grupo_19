package com.minimarket.security.retention;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class DataRetentionService {

    private final UsuarioRepository usuarioRepository;
    private final DataRetentionProperties properties;
    private final PasswordEncoder passwordEncoder;

    public DataRetentionService(
            UsuarioRepository usuarioRepository,
            DataRetentionProperties properties,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Scheduled(cron = "${data.retention.cron:0 0 2 * * *}")
    @Transactional
    public void runScheduledRetention() {
        if (properties.isEnabled()) {
            anonymizeInactiveUsers();
        }
    }

    @Transactional
    public int anonymizeInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(properties.getInactiveDays());
        List<Usuario> candidates = usuarioRepository
                .findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(threshold);

        for (Usuario usuario : candidates) {
            anonymize(usuario);
        }
        return candidates.size();
    }

    private void anonymize(Usuario usuario) {
        usuario.setUsername("anon_" + UUID.randomUUID().toString().substring(0, 8));
        usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setRoles(new HashSet<>());
        usuario.setTotpSecret(null);
        usuario.setMfaEnabled(false);
        usuario.setAnonymized(true);
        usuarioRepository.save(usuario);
    }
}
