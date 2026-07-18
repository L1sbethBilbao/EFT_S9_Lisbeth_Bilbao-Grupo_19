package com.minimarket.service.impl;

import com.minimarket.dto.reporte.RotacionProductoResponseDTO;
import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.DetallePedidoRepository;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    @DisplayName("GIVEN Ventas Y Pedidos WHEN Obtener Rotacion THEN Combina Cantidades")
    void givenVentasYPedidos_whenObtenerRotacion_thenCombinaCantidades() {
        Producto arroz = TestFixtures.productoArrozConStock(10);
        Producto leche = TestFixtures.productoConStock(11L, "Leche", 500.0, 8);

        DetalleVenta detalleVenta = new DetalleVenta();
        detalleVenta.setProducto(arroz);
        detalleVenta.setCantidad(5);
        detalleVenta.setVenta(new Venta());

        DetallePedido detallePedido = new DetallePedido();
        detallePedido.setProducto(arroz);
        detallePedido.setCantidad(3);
        detallePedido.setPedido(new Pedido());

        DetallePedido detallePedidoLeche = new DetallePedido();
        detallePedidoLeche.setProducto(leche);
        detallePedidoLeche.setCantidad(2);
        detallePedidoLeche.setPedido(new Pedido());

        when(detalleVentaRepository.findAll()).thenReturn(List.of(detalleVenta));
        when(detallePedidoRepository.findAll()).thenReturn(List.of(detallePedido, detallePedidoLeche));

        List<RotacionProductoResponseDTO> reporte = reporteService.obtenerRotacionProductos();

        assertThat(reporte).hasSize(2);
        assertThat(reporte.get(0).getProductoId()).isEqualTo(10L);
        assertThat(reporte.get(0).getCantidadVentas()).isEqualTo(5);
        assertThat(reporte.get(0).getCantidadPedidos()).isEqualTo(3);
        assertThat(reporte.get(0).getTotalRotacion()).isEqualTo(8);
    }

    @Test
    @DisplayName("GIVEN Sin Movimientos WHEN Obtener Rotacion THEN Retorna Vacio")
    void givenSinMovimientos_whenObtenerRotacion_thenRetornaVacio() {
        when(detalleVentaRepository.findAll()).thenReturn(List.of());
        when(detallePedidoRepository.findAll()).thenReturn(List.of());

        assertThat(reporteService.obtenerRotacionProductos()).isEmpty();
    }
}
