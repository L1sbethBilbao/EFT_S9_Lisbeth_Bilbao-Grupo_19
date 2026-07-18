package com.minimarket.service;

import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PedidoService {

    List<Pedido> findAll();

    Page<Pedido> findAll(Pageable pageable);

    Pedido findById(Long id);

    Pedido save(Pedido pedido);

    List<Pedido> findByUsuarioId(Long usuarioId);

    Pedido registrarPedido(PedidoRegistroDTO dto);

    Pedido actualizarEstado(Long id, String nuevoEstado);

    double calcularTotal(List<DetallePedido> detalles);
}
