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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

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
        producto = TestFixtures.productoArrozConStock(10);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista")
    void givenDefaultContext_whenFindAll_thenRetornaLista() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);
        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));

        assertThat(inventarioService.findAll()).containsExactly(inventario);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Inventario")
    void givenExiste_whenFindById_thenRetornaInventario() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        assertThat(inventarioService.findById(1L)).isSameAs(inventario);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(inventarioService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        inventarioService.deleteById(4L);

        verify(inventarioRepository).deleteById(4L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Producto Id THEN Retorna Movimientos")
    void givenDefaultContext_whenFindByProductoId_thenRetornaMovimientos() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);
        when(inventarioRepository.findByProductoId(8L)).thenReturn(List.of(inventario));

        assertThat(inventarioService.findByProductoId(8L)).containsExactly(inventario);
    }

    @Test
    @DisplayName("GIVEN Tipo Movimiento Nulo WHEN Validar Campos THEN Lanza Excepcion")
    void givenTipoMovimientoNulo_whenValidarCampos_thenLanzaExcepcion() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);
        inventario.setTipoMovimiento(null);

        assertThatThrownBy(() -> inventarioService.validarCamposMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BusinessErrorMessages.TIPO_MOVIMIENTO_OBLIGATORIO);
    }

    @Test
    @DisplayName("GIVEN Tipo Movimiento Vacio WHEN Validar Campos THEN Lanza Excepcion")
    void givenTipoMovimientoVacio_whenValidarCampos_thenLanzaExcepcion() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);
        inventario.setTipoMovimiento("   ");

        assertThatThrownBy(() -> inventarioService.validarCamposMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BusinessErrorMessages.TIPO_MOVIMIENTO_OBLIGATORIO);
    }

    @Test
    @DisplayName("GIVEN Cantidad Nula WHEN Validar Campos THEN Lanza Excepcion")
    void givenCantidadNula_whenValidarCampos_thenLanzaExcepcion() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);
        inventario.setCantidad(null);

        assertThatThrownBy(() -> inventarioService.validarCamposMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BusinessErrorMessages.CANTIDAD_OBLIGATORIA);
    }

    @Test
    @DisplayName("GIVEN Cantidad Cero WHEN Validar Campos THEN Lanza Excepcion")
    void givenCantidadCero_whenValidarCampos_thenLanzaExcepcion() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 0);

        assertThatThrownBy(() -> inventarioService.validarCamposMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(BusinessErrorMessages.CANTIDAD_OBLIGATORIA);
    }

    @Test
    @DisplayName("GIVEN Producto No Encontrado WHEN Validar Producto Asociado THEN Lanza Excepcion")
    void givenProductoNoEncontrado_whenValidarProductoAsociado_thenLanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.validarProductoAsociado(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    @DisplayName("GIVEN Movimiento Entrada WHEN Registrar Movimiento THEN Aumenta Stock Y Guarda")
    void givenMovimientoEntrada_whenRegistrarMovimiento_thenAumentaStockYGuarda() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 3);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0, String.class).trim());
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario saved = inventarioService.registrarMovimiento(inventario);

        assertThat(saved.getProducto()).isEqualTo(producto);
        assertThat(producto.getStock()).isEqualTo(13);
        assertThat(saved.getFechaMovimiento()).isNotNull();
        verify(productoRepository).save(producto);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    @DisplayName("GIVEN Movimiento Salida Con Stock WHEN Registrar Movimiento THEN Disminuye Stock")
    void givenMovimientoSalidaConStock_whenRegistrarMovimiento_thenDisminuyeStock() {
        Inventario inventario = TestFixtures.inventarioSalida(producto, 4);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0, String.class).trim());
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario saved = inventarioService.registrarMovimiento(inventario);

        assertThat(saved.getProducto()).isEqualTo(producto);
        assertThat(producto.getStock()).isEqualTo(6);
        verify(productoRepository).save(producto);
    }

    @Test
    @DisplayName("GIVEN Salida Sin Stock WHEN Registrar Movimiento THEN No Persiste Inventario")
    void givenSalidaSinStock_whenRegistrarMovimiento_thenNoPersisteInventario() {
        Inventario inventario = TestFixtures.inventarioSalida(producto, 20);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0, String.class).trim());
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> inventarioService.registrarMovimiento(inventario))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");

        verify(inventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Tipo Invalido WHEN Registrar Movimiento THEN Lanza Excepcion")
    void givenTipoInvalido_whenRegistrarMovimiento_thenLanzaExcepcion() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 2);
        inventario.setTipoMovimiento("Ajuste");
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0, String.class).trim());
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> inventarioService.registrarMovimiento(inventario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de movimiento no válido");

        verify(inventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Sanitiza Tipo Movimiento")
    void givenDefaultContext_whenSave_thenSanitizaTipoMovimiento() {
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 2);
        inventario.setTipoMovimiento("  Entrada  ");
        when(inputSanitizer.sanitize(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(inv -> inv.getArgument(0, String.class).trim());
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));

        Inventario saved = inventarioService.save(inventario);

        assertThat(saved.getTipoMovimiento()).isEqualTo("Entrada");
        verify(inputSanitizer, org.mockito.Mockito.atLeastOnce()).sanitize("  Entrada  ");
    }
}
