package com.minimarket.mapper;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
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
class CarritoMapperTest {

    @Autowired
    private CarritoMapper mapper;

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response THEN Mapea Carrito")
    void givenDefaultContext_whenToResponse_thenMapeaCarrito() {
        Usuario usuario = usuarioDemo();
        Producto producto = productoDemo();

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(2);

        var dto = mapper.toResponse(carrito);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCantidad()).isEqualTo(2);
        assertThat(dto.getUsuario().getUsername()).isEqualTo("cliente1");
        assertThat(dto.getProducto().getNombre()).isEqualTo("Arroz");
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response THEN Retorna Null")
    void givenNull_whenToResponse_thenRetornaNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response List THEN Mapea Lista")
    void givenDefaultContext_whenToResponseList_thenMapeaLista() {
        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setCantidad(1);

        assertThat(mapper.toResponseList(List.of(carrito))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response List THEN Retorna Null")
    void givenNull_whenToResponseList_thenRetornaNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Entity THEN Mapea Dto")
    void givenDefaultContext_whenToEntity_thenMapeaDto() {
        CarritoRequestDTO dto = new CarritoRequestDTO();
        dto.setId(3L);
        dto.setUsuario(new IdRefDTO(1L));
        dto.setProducto(new IdRefDTO(2L));
        dto.setCantidad(4);

        Carrito entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getCantidad()).isEqualTo(4);
        assertThat(entity.getUsuario().getId()).isEqualTo(1L);
        assertThat(entity.getProducto().getId()).isEqualTo(2L);
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

    @Test
    @DisplayName("GIVEN Ref Invalido WHEN Id Ref To Producto THEN Retorna Null")
    void givenRefInvalido_whenIdRefToProducto_thenRetornaNull() {
        assertThat(mapper.idRefToProducto(null)).isNull();
        assertThat(mapper.idRefToProducto(new IdRefDTO(null))).isNull();
    }

    private static Usuario usuarioDemo() {
        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre(SecurityRoles.CLIENTE);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");
        usuario.setNombre("Cliente");
        usuario.setRoles(Set.of(rol));
        return usuario;
    }

    private static Producto productoDemo() {
        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Arroz");
        producto.setPrecio(1000.0);
        producto.setStock(10);
        return producto;
    }
}
