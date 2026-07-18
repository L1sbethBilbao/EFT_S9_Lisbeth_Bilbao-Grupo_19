package com.minimarket.repository;

import com.minimarket.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    List<OrdenCompra> findBySucursalId(Long sucursalId);

    List<OrdenCompra> findByEstado(String estado);

    List<OrdenCompra> findBySucursalIdAndEstado(Long sucursalId, String estado);
}
