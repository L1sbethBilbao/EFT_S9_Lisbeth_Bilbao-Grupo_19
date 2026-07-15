package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.venta.VentaRequestDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.security.constants.SecurityRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperTestConfiguration.class)
class VentaMapperTest {

    @Autowired
    private VentaMapper mapper;

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response THEN Mapea Venta")
    void givenDefaultContext_whenToResponse_thenMapeaVenta() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre(SecurityRoles.CLIENTE);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");
        usuario.setRoles(Set.of(rol));

        Date fecha = new Date();
        Venta venta = new Venta();
        venta.setId(10L);
        venta.setUsuario(usuario);
        venta.setFecha(fecha);
        venta.setTotal(5990.0);

        var dto = mapper.toResponse(venta);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getUsuario().getUsername()).isEqualTo("cliente1");
        assertThat(dto.getFecha()).isEqualTo(fecha);
        assertThat(dto.getTotal()).isEqualTo(5990.0);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response THEN Retorna Null")
    void givenNull_whenToResponse_thenRetornaNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response List THEN Mapea Lista")
    void givenDefaultContext_whenToResponseList_thenMapeaLista() {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setTotal(1000.0);
        venta.setFecha(new Date());

        assertThat(mapper.toResponseList(List.of(venta))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response List THEN Retorna Null")
    void givenNull_whenToResponseList_thenRetornaNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Entity THEN Mapea Dto")
    void givenDefaultContext_whenToEntity_thenMapeaDto() {
        VentaRequestDTO dto = new VentaRequestDTO();
        dto.setId(5L);
        dto.setUsuario(new IdRefDTO(1L));
        dto.setFecha(new Date());

        Venta entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getUsuario().getId()).isEqualTo(1L);
        assertThat(entity.getFecha()).isEqualTo(dto.getFecha());
        assertThat(entity.getDetalles()).isNull();
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Entity THEN Retorna Null")
    void givenNull_whenToEntity_thenRetornaNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Ref Invalido WHEN Id Ref To Usuario THEN Retorna Null")
    void givenRefInvalido_whenIdRefToUsuario_thenRetornaNull() {
        assertThat(mapper.idRefToUsuario(null)).isNull();
        assertThat(mapper.idRefToUsuario(new IdRefDTO(null))).isNull();
    }
}
