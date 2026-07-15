package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.detalleventa.DetalleVentaRequestDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperTestConfiguration.class)
class DetalleVentaMapperTest {

    @Autowired
    private DetalleVentaMapper mapper;

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response THEN Mapea Detalle")
    void givenDefaultContext_whenToResponse_thenMapeaDetalle() {
        Venta venta = new Venta();
        venta.setId(10L);

        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Arroz");
        producto.setPrecio(1000.0);
        producto.setStock(5);
        producto.setCategoria(categoriaDemo());

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecio(2000.0);

        var dto = mapper.toResponse(detalle);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getVenta().getId()).isEqualTo(10L);
        assertThat(dto.getProducto().getNombre()).isEqualTo("Arroz");
        assertThat(dto.getCantidad()).isEqualTo(2);
        assertThat(dto.getPrecio()).isEqualTo(2000.0);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response THEN Retorna Null")
    void givenNull_whenToResponse_thenRetornaNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response List THEN Mapea Lista")
    void givenDefaultContext_whenToResponseList_thenMapeaLista() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setCantidad(1);
        detalle.setPrecio(1000.0);

        assertThat(mapper.toResponseList(List.of(detalle))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response List THEN Retorna Null")
    void givenNull_whenToResponseList_thenRetornaNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Entity THEN Mapea Dto")
    void givenDefaultContext_whenToEntity_thenMapeaDto() {
        DetalleVentaRequestDTO dto = new DetalleVentaRequestDTO();
        dto.setId(1L);
        dto.setVenta(new IdRefDTO(10L));
        dto.setProducto(new IdRefDTO(2L));
        dto.setCantidad(3);
        dto.setPrecio(1500.0);

        DetalleVenta entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getVenta().getId()).isEqualTo(10L);
        assertThat(entity.getProducto().getId()).isEqualTo(2L);
        assertThat(entity.getCantidad()).isEqualTo(3);
        assertThat(entity.getPrecio()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Entity THEN Retorna Null")
    void givenNull_whenToEntity_thenRetornaNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Null WHEN Venta To Id Ref THEN Retorna Null")
    void givenNull_whenVentaToIdRef_thenRetornaNull() {
        assertThat(mapper.ventaToIdRef(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Ref Invalido WHEN Id Ref To Venta THEN Retorna Null")
    void givenRefInvalido_whenIdRefToVenta_thenRetornaNull() {
        assertThat(mapper.idRefToVenta(null)).isNull();
        assertThat(mapper.idRefToVenta(new IdRefDTO(null))).isNull();
    }

    @Test
    @DisplayName("GIVEN Ref Invalido WHEN Id Ref To Producto THEN Retorna Null")
    void givenRefInvalido_whenIdRefToProducto_thenRetornaNull() {
        assertThat(mapper.idRefToProducto(null)).isNull();
        assertThat(mapper.idRefToProducto(new IdRefDTO(null))).isNull();
    }

    private static Categoria categoriaDemo() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");
        return categoria;
    }
}
