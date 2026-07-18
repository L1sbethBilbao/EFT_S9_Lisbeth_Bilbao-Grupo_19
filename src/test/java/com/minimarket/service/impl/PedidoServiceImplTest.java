package com.minimarket.service.impl;

import com.minimarket.constants.PedidoConstants;
import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.PedidoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.PromocionService;
import com.minimarket.service.StockSucursalService;
import com.minimarket.service.UsuarioService;
import com.minimarket.support.TestFixtures;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private StockSucursalService stockSucursalService;

    @Mock
    private PromocionService promocionService;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Usuario usuario;
    private Sucursal sucursal;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = TestFixtures.usuarioClienteCompleto();
        sucursal = TestFixtures.sucursalProvidencia();
        producto = TestFixtures.productoArrozConStock(10);
    }

    @Test
    @DisplayName("GIVEN Con Stock Suficiente WHEN Registrar Pedido THEN Guarda Pedido")
    void givenConStockSuficiente_whenRegistrarPedido_thenGuardaPedido() {
        PedidoRegistroDTO dto = TestFixtures.pedidoRegistro(1L, 1L, 10L, 2);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(promocionService.aplicarDescuento(10L, 1L, 1000.0)).thenReturn(900.0);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        Pedido result = pedidoService.registrarPedido(dto);

        assertThat(result.getTotal()).isEqualTo(1800.0);
        assertThat(result.getEstado()).isEqualTo(PedidoConstants.ESTADO_PENDIENTE);
        assertThat(result.getTipoEntrega()).isEqualTo(PedidoConstants.TIPO_RETIRO);
        verify(stockSucursalService).decrementarStock(1L, 10L, 2);
        verify(usuarioService).validarDatosCompletos(usuario);
    }

    @Test
    @DisplayName("GIVEN Stock Insuficiente WHEN Registrar Pedido THEN No Persiste")
    void givenStockInsuficiente_whenRegistrarPedido_thenNoPersiste() {
        PedidoRegistroDTO dto = TestFixtures.pedidoRegistro(1L, 1L, 10L, 20);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        doThrow(new StockInsuficienteException("sin stock"))
                .when(stockSucursalService).validarStockDisponible(1L, 10L, 20);

        assertThatThrownBy(() -> pedidoService.registrarPedido(dto))
                .isInstanceOf(StockInsuficienteException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Despacho Sin Direccion WHEN Registrar Pedido THEN Lanza Excepcion")
    void givenDespachoSinDireccion_whenRegistrarPedido_thenLanzaExcepcion() {
        PedidoRegistroDTO dto = TestFixtures.pedidoRegistro(1L, 1L, 10L, 1);
        dto.setTipoEntrega("DESPACHO");

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));

        assertThatThrownBy(() -> pedidoService.registrarPedido(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dirección de entrega");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Calcular Total THEN Suma Subtotales")
    void givenDefaultContext_whenCalcularTotal_thenSumaSubtotales() {
        DetallePedido detalle = new DetallePedido();
        detalle.setSubtotal(1500.0);

        assertThat(pedidoService.calcularTotal(List.of(detalle))).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("GIVEN Pedido Existe WHEN Actualizar Estado THEN Actualiza")
    void givenPedidoExiste_whenActualizarEstado_thenActualiza() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido result = pedidoService.actualizarEstado(1L, PedidoConstants.ESTADO_CONFIRMADO);

        assertThat(result.getEstado()).isEqualTo(PedidoConstants.ESTADO_CONFIRMADO);
    }

    @Test
    @DisplayName("GIVEN Estado Invalido WHEN Actualizar Estado THEN Lanza Excepcion")
    void givenEstadoInvalido_whenActualizarEstado_thenLanzaExcepcion() {
        Pedido pedido = new Pedido();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.actualizarEstado(1L, "INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
