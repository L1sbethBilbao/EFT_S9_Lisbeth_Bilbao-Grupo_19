package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.minimarket.service.impl.UsuarioServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioRolRelacionTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Nombre Rol THEN Usuario Tiene Rol Asociado")
    void givenDefaultContext_whenFindByNombreRol_thenUsuarioTieneRolAsociado() {
        Rol gerente = new Rol();
        gerente.setId(3L);
        gerente.setNombre(SecurityRoles.GERENTE);

        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("gerente1");
        u.setRoles(new HashSet<>(Set.of(gerente)));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(inputSanitizer.sanitize(anyString())).thenAnswer(inv -> inv.getArgument(0));

        Usuario found = usuarioService.findById(1L).orElseThrow();
        assertThat(found.getRoles()).extracting(Rol::getNombre).contains(SecurityRoles.GERENTE);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Venta Usuario THEN Relacion Bidireccional")
    void givenDefaultContext_whenVentaUsuario_thenRelacionBidireccional() {
        Rol cliente = new Rol();
        cliente.setNombre(SecurityRoles.CLIENTE);
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setRoles(Set.of(cliente));

        assertThat(usuario.getRoles()).isNotEmpty();
        assertThat(List.copyOf(usuario.getRoles()).get(0).getNombre()).isEqualTo(SecurityRoles.CLIENTE);
    }
}
