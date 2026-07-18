package com.minimarket.service;

import com.minimarket.entity.Sucursal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SucursalService {

    List<Sucursal> findAll();

    Page<Sucursal> findAll(Pageable pageable);

    Sucursal findById(Long id);

    Sucursal save(Sucursal sucursal);

    void deleteById(Long id);
}
