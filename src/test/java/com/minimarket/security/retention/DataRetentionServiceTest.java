package com.minimarket.security.retention;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataRetentionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DataRetentionProperties properties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataRetentionService dataRetentionService;

    @Test
    @DisplayName("GIVEN Deshabilitado WHEN Run Scheduled Retention THEN No Anonimiza")
    void givenDeshabilitado_whenRunScheduledRetention_thenNoAnonimiza() {
        when(properties.isEnabled()).thenReturn(false);

        dataRetentionService.runScheduledRetention();

        verify(usuarioRepository, never()).findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(any());
    }

    @Test
    @DisplayName("GIVEN Habilitado WHEN Run Scheduled Retention THEN Ejecuta Anonimizacion")
    void givenHabilitado_whenRunScheduledRetention_thenEjecutaAnonimizacion() {
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getInactiveDays()).thenReturn(90);
        when(usuarioRepository.findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(any()))
                .thenReturn(List.of());

        dataRetentionService.runScheduledRetention();

        verify(usuarioRepository).findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(any());
    }

    @Test
    @DisplayName("GIVEN Sin Candidatos WHEN Anonymize Inactive Users THEN Retorna Cero")
    void givenSinCandidatos_whenAnonymizeInactiveUsers_thenRetornaCero() {
        when(properties.getInactiveDays()).thenReturn(90);
        when(usuarioRepository.findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(any()))
                .thenReturn(List.of());

        assertThat(dataRetentionService.anonymizeInactiveUsers()).isZero();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Anonymize Inactive Users THEN Anonimiza Usuarios Inactivos")
    void givenDefaultContext_whenAnonymizeInactiveUsers_thenAnonimizaUsuariosInactivos() {
        Rol cliente = new Rol();
        cliente.setNombre(SecurityRoles.CLIENTE);

        Usuario usuario = new Usuario();
        usuario.setUsername("inactive_user");
        usuario.setPassword("hash");
        usuario.setRoles(new HashSet<>(Set.of(cliente)));
        usuario.setTotpSecret("secret");
        usuario.setMfaEnabled(true);
        usuario.setLastLoginAt(LocalDateTime.now().minusDays(100));

        when(properties.getInactiveDays()).thenReturn(90);
        when(usuarioRepository.findByAnonymizedFalseAndRetentionExcludedFalseAndLastLoginAtBefore(any()))
                .thenReturn(List.of(usuario));
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        int count = dataRetentionService.anonymizeInactiveUsers();

        assertThat(count).isEqualTo(1);
        assertThat(usuario.isAnonymized()).isTrue();
        assertThat(usuario.getUsername()).startsWith("anon_");
        assertThat(usuario.getPassword()).isEqualTo("encoded-password");
        assertThat(usuario.getRoles()).isEmpty();
        assertThat(usuario.getTotpSecret()).isNull();
        assertThat(usuario.isMfaEnabled()).isFalse();
        verify(usuarioRepository).save(usuario);
    }
}
