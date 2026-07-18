package com.minimarket.service;

import com.minimarket.dto.stocksucursal.DisponibilidadResponseDTO;
import com.minimarket.entity.StockSucursal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockSucursalService {

    List<StockSucursal> findAll();

    Page<StockSucursal> findAll(Pageable pageable);

    StockSucursal findById(Long id);

    StockSucursal save(StockSucursal stockSucursal);

    void deleteById(Long id);

    List<StockSucursal> findBySucursalId(Long sucursalId);

    List<DisponibilidadResponseDTO> consultarDisponibilidad(Long sucursalId);

    void validarStockDisponible(Long sucursalId, Long productoId, int cantidad);

    void decrementarStock(Long sucursalId, Long productoId, int cantidad);

    void incrementarStock(Long sucursalId, Long productoId, int cantidad);
}
