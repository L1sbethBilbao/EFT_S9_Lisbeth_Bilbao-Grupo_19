package com.minimarket.dto;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.dto.pedido.PedidoItemDTO;
import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.dto.promocion.PromocionRequestDTO;
import com.minimarket.dto.reporte.RotacionProductoResponseDTO;
import com.minimarket.dto.stocksucursal.StockSucursalRequestDTO;
import com.minimarket.dto.sucursal.SucursalRequestDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;
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
    @DisplayName("GIVEN Pedido Despacho Sin Direccion WHEN Validar THEN Falla")
    void givenPedidoDespachoSinDireccion_whenValidar_thenFalla() {
        PedidoRegistroDTO dto = new PedidoRegistroDTO();
        dto.setUsuarioId(1L);
        dto.setSucursalId(1L);
        dto.setTipoEntrega("DESPACHO");
        PedidoItemDTO item = new PedidoItemDTO();
        item.setProductoId(1L);
        item.setCantidad(1);
        dto.setItems(List.of(item));

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN Sucursal Sin Nombre WHEN Validar THEN Falla")
    void givenSucursalSinNombre_whenValidar_thenFalla() {
        SucursalRequestDTO dto = new SucursalRequestDTO();
        dto.setDireccion("Av 1");
        dto.setComuna("Santiago");

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Promocion Descuento Invalido WHEN Validar THEN Falla")
    void givenPromocionDescuentoInvalido_whenValidar_thenFalla() {
        PromocionRequestDTO dto = new PromocionRequestDTO();
        dto.setNombre("Oferta");
        dto.setDescuentoPorcentaje(150.0);
        dto.setFechaInicio(new java.util.Date());
        dto.setFechaFin(new java.util.Date());

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Stock Sucursal Sin Producto WHEN Validar THEN Falla")
    void givenStockSucursalSinProducto_whenValidar_thenFalla() {
        StockSucursalRequestDTO dto = new StockSucursalRequestDTO();
        dto.setSucursal(new IdRefDTO(1L));
        dto.setCantidad(5);
        dto.setStockMinimo(2);

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    @DisplayName("GIVEN Dtos Semana9 Validos WHEN Getters Setters THEN Funcionan")
    void givenDtosSemana9Validos_whenGettersSetters_thenFuncionan() {
        SucursalResponseDTO sucursal = new SucursalResponseDTO();
        sucursal.setId(1L);
        sucursal.setNombre("Centro");
        sucursal.setDireccion("Av 1");
        sucursal.setComuna("Santiago");
        sucursal.setActiva(true);

        RotacionProductoResponseDTO rotacion = new RotacionProductoResponseDTO();
        rotacion.setProductoId(1L);
        rotacion.setProductoNombre("Arroz");
        rotacion.setCantidadVentas(5);
        rotacion.setCantidadPedidos(3);
        rotacion.setTotalRotacion(8);

        assertThat(sucursal.getNombre()).isEqualTo("Centro");
        assertThat(rotacion.getTotalRotacion()).isEqualTo(8);
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
