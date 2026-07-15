package com.minimarket.service;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CarritoService {
    List<Carrito> findAll();
    Page<Carrito> findAll(Pageable pageable);
    Carrito findById(Long id);
    Carrito save(Carrito carrito);
    void deleteById(Long id);
    List<Carrito> findByUsuarioId(Long usuarioId);
    Carrito agregarProducto(Long usuarioId, Long productoId, int cantidad);
    Usuario validarUsuarioCarrito(Long usuarioId);
    void validarStockDisponible(Long productoId, int cantidad);
}
