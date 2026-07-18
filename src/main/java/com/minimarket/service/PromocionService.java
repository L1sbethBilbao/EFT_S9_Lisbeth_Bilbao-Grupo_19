package com.minimarket.service;

import com.minimarket.entity.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PromocionService {

    List<Promocion> findAll();

    Page<Promocion> findAll(Pageable pageable);

    Promocion findById(Long id);

    Promocion save(Promocion promocion);

    void deleteById(Long id);

    List<Promocion> findActivas();

    double aplicarDescuento(Long productoId, Long sucursalId, double precioOriginal);
}
