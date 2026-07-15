package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperTestConfiguration.class)
class ProductoMapperTest {

    @Autowired
    private ProductoMapper mapper;

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response THEN Mapea Producto")
    void givenDefaultContext_whenToResponse_thenMapeaProducto() {
        Producto producto = productoDemo();

        var dto = mapper.toResponse(producto);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Arroz");
        assertThat(dto.getPrecio()).isEqualTo(1890.0);
        assertThat(dto.getStock()).isEqualTo(20);
        assertThat(dto.getDescripcion()).isEqualTo("Grano largo");
        assertThat(dto.getCategoria().getNombre()).isEqualTo("Abarrotes");
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response THEN Retorna Null")
    void givenNull_whenToResponse_thenRetornaNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Response List THEN Mapea Lista")
    void givenDefaultContext_whenToResponseList_thenMapeaLista() {
        assertThat(mapper.toResponseList(List.of(productoDemo()))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Response List THEN Retorna Null")
    void givenNull_whenToResponseList_thenRetornaNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN To Entity THEN Mapea Dto")
    void givenDefaultContext_whenToEntity_thenMapeaDto() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setId(2L);
        dto.setNombre("Atún");
        dto.setPrecio(1290.0);
        dto.setStock(15);
        dto.setDescripcion("En lata");
        dto.setCategoria(new IdRefDTO(1L));

        Producto entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getNombre()).isEqualTo("Atún");
        assertThat(entity.getCategoria().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN Null WHEN To Entity THEN Retorna Null")
    void givenNull_whenToEntity_thenRetornaNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Ref Invalido WHEN Id Ref To Categoria THEN Retorna Null")
    void givenRefInvalido_whenIdRefToCategoria_thenRetornaNull() {
        assertThat(mapper.idRefToCategoria(null)).isNull();
        assertThat(mapper.idRefToCategoria(new IdRefDTO(null))).isNull();
    }

    private static Producto productoDemo() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1890.0);
        producto.setStock(20);
        producto.setDescripcion("Grano largo");
        producto.setCategoria(categoria);
        return producto;
    }
}
