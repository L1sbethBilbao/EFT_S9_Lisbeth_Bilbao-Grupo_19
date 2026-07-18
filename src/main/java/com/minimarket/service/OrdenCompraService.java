package com.minimarket.service;

import com.minimarket.entity.OrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrdenCompraService {

    List<OrdenCompra> findAll();

    Page<OrdenCompra> findAll(Pageable pageable);

    OrdenCompra findById(Long id);

    OrdenCompra save(OrdenCompra ordenCompra);

    List<OrdenCompra> generarOrdenesAutomaticas();

    OrdenCompra confirmarRecepcion(Long ordenCompraId);
}
