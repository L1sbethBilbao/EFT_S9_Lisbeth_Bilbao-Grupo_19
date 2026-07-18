package com.minimarket.repository;

import com.minimarket.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivaTrue();

    List<Sucursal> findByComuna(String comuna);

    List<Sucursal> findByComunaAndActivaTrue(String comuna);
}
