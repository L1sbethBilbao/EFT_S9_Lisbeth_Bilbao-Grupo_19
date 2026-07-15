package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperTestConfiguration.class)
class InventarioMapperTest {

    @Autowired
    private InventarioMapper mapper;

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response THEN Mapea Inventario")
    void givenDefaultContext_whenToResponse_thenMapeaInventario() {
        Producto producto = productoDemo();
        Date fecha = new Date();

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(fecha);

        var dto = mapper.toResponse(inventario);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getProducto().getNombre()).isEqualTo("Arroz");
        assertThat(dto.getCantidad()).isEqualTo(10);
        assertThat(dto.getTipoMovimiento()).isEqualTo("Entrada");
        assertThat(dto.getFechaMovimiento()).isEqualTo(fecha);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response THEN Retorna Null")
    void givenNull_whenToResponse_thenRetornaNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response List THEN Mapea Lista")
    void givenDefaultContext_whenToResponseList_thenMapeaLista() {
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(5);
        inventario.setTipoMovimiento("Salida");
        inventario.setFechaMovimiento(new Date());

        assertThat(mapper.toResponseList(List.of(inventario))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response List THEN Retorna Null")
    void givenNull_whenToResponseList_thenRetornaNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Entity THEN Mapea Dto")
    void givenDefaultContext_whenToEntity_thenMapeaDto() {
        InventarioRequestDTO dto = new InventarioRequestDTO();
        dto.setId(2L);
        dto.setProducto(new IdRefDTO(3L));
        dto.setCantidad(7);
        dto.setTipoMovimiento("Entrada");
        dto.setFechaMovimiento(new Date());

        Inventario entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getProducto().getId()).isEqualTo(3L);
        assertThat(entity.getCantidad()).isEqualTo(7);
        assertThat(entity.getTipoMovimiento()).isEqualTo("Entrada");
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Entity THEN Retorna Null")
    void givenNull_whenToEntity_thenRetornaNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Ref Invalido WHEN Id Ref To Producto THEN Retorna Null")
    void givenRefInvalido_whenIdRefToProducto_thenRetornaNull() {
        assertThat(mapper.idRefToProducto(null)).isNull();
        assertThat(mapper.idRefToProducto(new IdRefDTO(null))).isNull();
    }

    private static Producto productoDemo() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");

        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Arroz");
        producto.setPrecio(1000.0);
        producto.setStock(10);
        producto.setCategoria(categoria);
        return producto;
    }
}
