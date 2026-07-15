package com.minimarket.mapper;

import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.security.constants.SecurityRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperTestConfiguration.class)
class UsuarioMapperTest {

    @Autowired
    private UsuarioMapper mapper;

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response THEN Mapea Usuario Con Roles")
    void givenDefaultContext_whenToResponse_thenMapeaUsuarioConRoles() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre(SecurityRoles.CLIENTE);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");
        usuario.setNombre("Cliente");
        usuario.setApellido("Demo");
        usuario.setEmail("cliente1@minimarket.cl");
        usuario.setDireccion("Av. Principal");
        usuario.setMfaEnabled(true);
        usuario.setRoles(Set.of(rol));

        var dto = mapper.toResponse(usuario);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("cliente1");
        assertThat(dto.getRoles()).hasSize(1);
        assertThat(dto.getRoles().get(0).getNombre()).isEqualTo(SecurityRoles.CLIENTE);
        assertThat(dto.isMfaEnabled()).isTrue();
    }

    @Test
    @DisplayName("GIVEN Sin Roles WHEN To Response THEN Retorna Lista Vacia")
    void givenSinRoles_whenToResponse_thenRetornaListaVacia() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setUsername("sin-roles");

        assertThat(mapper.toResponse(usuario).getRoles()).isEmpty();
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response THEN Retorna Null")
    void givenNull_whenToResponse_thenRetornaNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response List THEN Mapea Lista")
    void givenDefaultContext_whenToResponseList_thenMapeaLista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");

        assertThat(mapper.toResponseList(List.of(usuario))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response List THEN Retorna Null")
    void givenNull_whenToResponseList_thenRetornaNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Entity THEN Mapea Dto")
    void givenDefaultContext_whenToEntity_thenMapeaDto() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setId(3L);
        dto.setUsername("nuevo");
        dto.setPassword("hash");
        dto.setNombre("Nuevo");
        dto.setApellido("Usuario");
        dto.setEmail("nuevo@minimarket.cl");
        dto.setDireccion("Calle 1");

        Usuario entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getUsername()).isEqualTo("nuevo");
        assertThat(entity.getPassword()).isEqualTo("hash");
        assertThat(entity.getNombre()).isEqualTo("Nuevo");
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Entity THEN Retorna Null")
    void givenNull_whenToEntity_thenRetornaNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Null WHEN Roles To Dto THEN Retorna Lista Vacia")
    void givenNull_whenRolesToDto_thenRetornaListaVacia() {
        assertThat(mapper.rolesToDto(null)).isEmpty();
    }
}
