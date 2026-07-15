package com.minimarket.service;

import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.entity.Venta;

import java.util.List;

public interface VentaService {
    List<Venta> findAll();
    Venta findById(Long id);
    Venta save(Venta venta);
    List<Venta> findByUsuarioId(Long usuarioId);
    Venta registrarVenta(VentaRegistroDTO dto);
    double calcularTotal(List<com.minimarket.entity.DetalleVenta> detalles);
    void validarStockDisponible(Long productoId, int cantidad);
}
