package com.minimarket.repository;

import com.minimarket.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    List<Promocion> findByActivaTrue();

    @Query("SELECT p FROM Promocion p WHERE p.activa = true AND p.fechaInicio <= :ahora AND p.fechaFin >= :ahora")
    List<Promocion> findActivasEnFecha(@Param("ahora") Date ahora);

    List<Promocion> findByProductoId(Long productoId);

    List<Promocion> findBySucursalId(Long sucursalId);
}
