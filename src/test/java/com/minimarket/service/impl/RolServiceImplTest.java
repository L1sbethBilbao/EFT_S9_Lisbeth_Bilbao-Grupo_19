package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.repository.RolRepository;
import com.minimarket.security.constants.SecurityRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Nombre THEN Retorna Rol")
    void givenExiste_whenFindByNombre_thenRetornaRol() {
        Rol rol = new Rol();
        rol.setNombre(SecurityRoles.CLIENTE);
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE)).thenReturn(Optional.of(rol));

        assertThat(rolService.findByNombre(SecurityRoles.CLIENTE)).contains(rol);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Nombre THEN Retorna Vacio")
    void givenNoExiste_whenFindByNombre_thenRetornaVacio() {
        when(rolRepository.findByNombre("INEXISTENTE")).thenReturn(Optional.empty());

        assertThat(rolService.findByNombre("INEXISTENTE")).isEmpty();
    }
}
