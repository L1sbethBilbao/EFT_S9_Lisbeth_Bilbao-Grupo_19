package com.minimarket.service.impl;

import com.minimarket.constants.OrdenCompraConstants;
import com.minimarket.entity.DetalleOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.repository.OrdenCompraRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.service.StockSucursalService;
import com.minimarket.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceImplTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private StockSucursalRepository stockSucursalRepository;

    @Mock
    private StockSucursalService stockSucursalService;

    @InjectMocks
    private OrdenCompraServiceImpl ordenCompraService;

    @Test
    @DisplayName("GIVEN Stock Bajo Minimo WHEN Generar Ordenes THEN Crea Ordenes")
    void givenStockBajoMinimo_whenGenerarOrdenes_thenCreaOrdenes() {
        Producto producto = TestFixtures.productoArrozConStock(3);
        Sucursal sucursal = TestFixtures.sucursalProvidencia();
        StockSucursal stock = TestFixtures.stockSucursal(producto, sucursal, 3, 10);

        when(stockSucursalRepository.findAllBajoMinimo()).thenReturn(List.of(stock));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> {
            OrdenCompra orden = inv.getArgument(0);
            orden.setId(1L);
            return orden;
        });

        List<OrdenCompra> ordenes = ordenCompraService.generarOrdenesAutomaticas();

        assertThat(ordenes).hasSize(1);
        assertThat(ordenes.get(0).getEstado()).isEqualTo(OrdenCompraConstants.ESTADO_PENDIENTE);
        assertThat(ordenes.get(0).getDetalles()).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Sin Stock Bajo Minimo WHEN Generar Ordenes THEN Retorna Vacio")
    void givenSinStockBajoMinimo_whenGenerarOrdenes_thenRetornaVacio() {
        when(stockSucursalRepository.findAllBajoMinimo()).thenReturn(List.of());

        assertThat(ordenCompraService.generarOrdenesAutomaticas()).isEmpty();
    }

    @Test
    @DisplayName("GIVEN Orden Pendiente WHEN Confirmar Recepcion THEN Incrementa Stock")
    void givenOrdenPendiente_whenConfirmarRecepcion_thenIncrementaStock() {
        Producto producto = TestFixtures.productoArrozConStock(5);
        Sucursal sucursal = TestFixtures.sucursalProvidencia();
        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setProducto(producto);
        detalle.setCantidad(10);
        detalle.setCostoUnitario(700.0);

        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setSucursal(sucursal);
        orden.setEstado(OrdenCompraConstants.ESTADO_PENDIENTE);
        orden.setDetalles(new ArrayList<>(List.of(detalle)));
        detalle.setOrdenCompra(orden);

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenCompraRepository.save(orden)).thenReturn(orden);

        OrdenCompra result = ordenCompraService.confirmarRecepcion(1L);

        assertThat(result.getEstado()).isEqualTo(OrdenCompraConstants.ESTADO_RECIBIDA);
        verify(stockSucursalService).incrementarStock(1L, 10L, 10);
    }

    @Test
    @DisplayName("GIVEN Orden Ya Recibida WHEN Confirmar Recepcion THEN Lanza Excepcion")
    void givenOrdenYaRecibida_whenConfirmarRecepcion_thenLanzaExcepcion() {
        OrdenCompra orden = new OrdenCompra();
        orden.setEstado(OrdenCompraConstants.ESTADO_RECIBIDA);
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThatThrownBy(() -> ordenCompraService.confirmarRecepcion(1L))
                .isInstanceOf(IllegalStateException.class);
    }
}
