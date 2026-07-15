package com.minimarket.service;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventarioService {
    List<Inventario> findAll();
    Page<Inventario> findAll(Pageable pageable);
    Inventario findById(Long id);
    Inventario save(Inventario inventario);
    void deleteById(Long id);
    List<Inventario> findByProductoId(Long productoId);
    Inventario registrarMovimiento(Inventario inventario);
    void validarCamposMovimiento(Inventario inventario);
    Producto validarProductoAsociado(Long productoId);
}
