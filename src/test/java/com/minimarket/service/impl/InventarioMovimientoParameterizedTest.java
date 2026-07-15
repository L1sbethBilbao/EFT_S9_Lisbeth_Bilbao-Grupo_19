package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.support.TestFixtures;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioMovimientoParameterizedTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = TestFixtures.productoArrozConStock(50);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0, String.class).trim());
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @ParameterizedTest(name = "tipo={0}, cantidad={1}")
    @CsvSource({
            "Entrada, 5",
            "Entrada, 10",
            "Salida, 3",
            "Salida, 1"
    })
    @DisplayName("GIVEN Tipos De Movimiento WHEN Registrar Movimiento THEN Completa Sin Error")
    void givenTiposDeMovimiento_whenRegistrarMovimiento_thenCompletaSinError(String tipoMovimiento, int cantidad) {
        Inventario inventario = "Entrada".equals(tipoMovimiento)
                ? TestFixtures.inventarioEntrada(producto, cantidad)
                : TestFixtures.inventarioSalida(producto, cantidad);

        assertThatCode(() -> inventarioService.registrarMovimiento(inventario))
                .doesNotThrowAnyException();
    }
}
