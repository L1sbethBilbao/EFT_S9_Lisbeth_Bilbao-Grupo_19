package com.minimarket.dto;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.dto.venta.VentaItemDTO;
import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.security.model.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("GIVEN Venta Registro Sin Items WHEN Validar THEN Falla")
    void givenVentaRegistroSinItems_whenValidar_thenFalla() {
        VentaRegistroDTO dto = new VentaRegistroDTO();
        dto.setUsuarioId(1L);
        dto.setItems(List.of());

        Set<ConstraintViolation<VentaRegistroDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Venta Item Cantidad Cero WHEN Validar THEN Falla")
    void givenVentaItemCantidadCero_whenValidar_thenFalla() {
        VentaItemDTO item = new VentaItemDTO();
        item.setProductoId(1L);
        item.setCantidad(0);

        Set<ConstraintViolation<VentaItemDTO>> violations = validator.validate(item);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Carrito Sin Usuario WHEN Validar THEN Falla")
    void givenCarritoSinUsuario_whenValidar_thenFalla() {
        CarritoRequestDTO dto = new CarritoRequestDTO();
        dto.setProducto(new IdRefDTO(1L));
        dto.setCantidad(1);

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Inventario Cantidad Negativa WHEN Validar THEN Falla")
    void givenInventarioCantidadNegativa_whenValidar_thenFalla() {
        InventarioRequestDTO dto = new InventarioRequestDTO();
        dto.setProducto(new IdRefDTO(1L));
        dto.setCantidad(0);
        dto.setTipoMovimiento("Entrada");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Producto Precio Negativo WHEN Validar THEN Falla")
    void givenProductoPrecioNegativo_whenValidar_thenFalla() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Test");
        dto.setPrecio(-1.0);
        dto.setStock(1);
        dto.setCategoria(new IdRefDTO(1L));

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Login Sin Password WHEN Validar THEN Falla")
    void givenLoginSinPassword_whenValidar_thenFalla() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user1");

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Dtos Validos WHEN Validar THEN Sin Violaciones")
    void givenDtosValidos_whenValidar_thenSinViolaciones() {
        VentaRegistroDTO venta = new VentaRegistroDTO();
        venta.setUsuarioId(1L);
        VentaItemDTO item = new VentaItemDTO();
        item.setProductoId(1L);
        item.setCantidad(2);
        venta.setItems(List.of(item));

        CarritoRequestDTO carrito = new CarritoRequestDTO();
        carrito.setUsuario(new IdRefDTO(1L));
        carrito.setProducto(new IdRefDTO(1L));
        carrito.setCantidad(1);

        LoginRequest login = new LoginRequest();
        login.setUsername("user1");
        login.setPassword("secret123");

        assertThat(validator.validate(venta)).isEmpty();
        assertThat(validator.validate(carrito)).isEmpty();
        assertThat(validator.validate(login)).isEmpty();
    }
}
