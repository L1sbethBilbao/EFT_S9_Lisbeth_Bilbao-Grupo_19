package com.minimarket.service.impl;

import com.minimarket.dto.reporte.RotacionProductoResponseDTO;
import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.repository.DetallePedidoRepository;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Override
    public List<RotacionProductoResponseDTO> obtenerRotacionProductos() {
        Map<Long, RotacionProductoResponseDTO> rotacionPorProducto = new HashMap<>();

        for (DetalleVenta detalle : detalleVentaRepository.findAll()) {
            agregarVenta(rotacionPorProducto, detalle);
        }

        for (DetallePedido detalle : detallePedidoRepository.findAll()) {
            agregarPedido(rotacionPorProducto, detalle);
        }

        return rotacionPorProducto.values().stream()
                .peek(this::calcularTotalRotacion)
                .sorted(Comparator.comparing(RotacionProductoResponseDTO::getTotalRotacion).reversed())
                .toList();
    }

    private void agregarVenta(Map<Long, RotacionProductoResponseDTO> mapa, DetalleVenta detalle) {
        Producto producto = detalle.getProducto();
        if (producto == null) {
            return;
        }
        RotacionProductoResponseDTO dto = mapa.computeIfAbsent(producto.getId(), id -> crearDto(producto));
        int actual = dto.getCantidadVentas() != null ? dto.getCantidadVentas() : 0;
        dto.setCantidadVentas(actual + detalle.getCantidad());
    }

    private void agregarPedido(Map<Long, RotacionProductoResponseDTO> mapa, DetallePedido detalle) {
        Producto producto = detalle.getProducto();
        if (producto == null) {
            return;
        }
        RotacionProductoResponseDTO dto = mapa.computeIfAbsent(producto.getId(), id -> crearDto(producto));
        int actual = dto.getCantidadPedidos() != null ? dto.getCantidadPedidos() : 0;
        dto.setCantidadPedidos(actual + detalle.getCantidad());
    }

    private RotacionProductoResponseDTO crearDto(Producto producto) {
        RotacionProductoResponseDTO dto = new RotacionProductoResponseDTO();
        dto.setProductoId(producto.getId());
        dto.setProductoNombre(producto.getNombre());
        dto.setCantidadVentas(0);
        dto.setCantidadPedidos(0);
        dto.setTotalRotacion(0);
        return dto;
    }

    private void calcularTotalRotacion(RotacionProductoResponseDTO dto) {
        int ventas = dto.getCantidadVentas() != null ? dto.getCantidadVentas() : 0;
        int pedidos = dto.getCantidadPedidos() != null ? dto.getCantidadPedidos() : 0;
        dto.setTotalRotacion(ventas + pedidos);
    }
}
