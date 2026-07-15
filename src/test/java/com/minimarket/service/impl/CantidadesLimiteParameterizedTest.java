package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.support.TestFixtures;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CantidadesLimiteParameterizedTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = TestFixtures.productoArrozConStock(5);
    }

    @ParameterizedTest(name = "cantidad={0}")
    @CsvSource({
            "0",
            "-1"
    })
    @DisplayName("GIVEN Cantidad Invalida WHEN Validar Campos Inventario THEN Lanza Excepcion Con Mensaje")
    void givenCantidadInvalida_whenValidarCamposInventario_thenLanzaExcepcionConMensaje(int cantidad) {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, Math.max(cantidad, 0));
        inventario.setCantidad(cantidad == 0 ? 0 : cantidad);

        assertThatThrownBy(() -> inventarioService.validarCamposMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BusinessErrorMessages.CANTIDAD_OBLIGATORIA);
    }

    @Test
    @DisplayName("GIVEN Cantidad Nula WHEN Validar Campos Inventario THEN Lanza Excepcion Con Mensaje")
    void givenCantidadNula_whenValidarCamposInventario_thenLanzaExcepcionConMensaje() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 1);
        inventario.setCantidad(null);

        assertThatThrownBy(() -> inventarioService.validarCamposMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BusinessErrorMessages.CANTIDAD_OBLIGATORIA);
    }

    @ParameterizedTest(name = "cantidad={0}")
    @ValueSource(ints = {10, 6})
    @DisplayName("GIVEN Cantidad Mayor Al Stock WHEN Validar Stock Carrito THEN Lanza StockInsuficiente")
    void givenCantidadMayorAlStock_whenValidarStockCarrito_thenLanzaStockInsuficiente(int cantidad) {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> carritoService.validarStockDisponible(10L, cantidad))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");
    }

    @ParameterizedTest(name = "cantidad={0}")
    @ValueSource(ints = {1, 5})
    @DisplayName("GIVEN Cantidad Valida WHEN Validar Stock Carrito THEN No Lanza Excepcion")
    void givenCantidadValida_whenValidarStockCarrito_thenNoLanzaExcepcion(int cantidad) {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatCode(() -> carritoService.validarStockDisponible(10L, cantidad))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "cantidad={0}")
    @ValueSource(ints = {10, 6})
    @DisplayName("GIVEN Cantidad Mayor Al Stock WHEN Validar Stock Venta THEN Lanza StockInsuficiente")
    void givenCantidadMayorAlStock_whenValidarStockVenta_thenLanzaStockInsuficiente(int cantidad) {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> ventaService.validarStockDisponible(10L, cantidad))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");
    }

    @ParameterizedTest(name = "cantidad={0}")
    @ValueSource(ints = {1, 5})
    @DisplayName("GIVEN Cantidad Valida WHEN Validar Stock Venta THEN No Lanza Excepcion")
    void givenCantidadValida_whenValidarStockVenta_thenNoLanzaExcepcion(int cantidad) {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatCode(() -> ventaService.validarStockDisponible(10L, cantidad))
                .doesNotThrowAnyException();
    }
}
