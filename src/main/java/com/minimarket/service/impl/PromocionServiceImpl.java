package com.minimarket.service.impl;

import com.minimarket.entity.Promocion;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.service.PromocionService;
import com.minimarket.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PromocionServiceImpl implements PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public List<Promocion> findAll() {
        return promocionRepository.findAll().stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    @Override
    public Page<Promocion> findAll(Pageable pageable) {
        return promocionRepository.findAll(pageable).map(promocion -> {
            sanitizeForOutput(promocion);
            return promocion;
        });
    }

    @Override
    public Promocion findById(Long id) {
        Promocion promocion = promocionRepository.findById(id).orElse(null);
        if (promocion != null) {
            sanitizeForOutput(promocion);
        }
        return promocion;
    }

    @Override
    public Promocion save(Promocion promocion) {
        sanitizeForInput(promocion);
        Promocion saved = promocionRepository.save(promocion);
        sanitizeForOutput(saved);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        promocionRepository.deleteById(id);
    }

    @Override
    public List<Promocion> findActivas() {
        return promocionRepository.findActivasEnFecha(new Date()).stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    @Override
    public double aplicarDescuento(Long productoId, Long sucursalId, double precioOriginal) {
        List<Promocion> activas = promocionRepository.findActivasEnFecha(new Date());
        double mejorPrecio = precioOriginal;

        for (Promocion promocion : activas) {
            if (!aplicaPromocion(promocion, productoId, sucursalId)) {
                continue;
            }
            double descuento = promocion.getDescuentoPorcentaje() / 100.0;
            double precioConDescuento = precioOriginal * (1.0 - descuento);
            if (precioConDescuento < mejorPrecio) {
                mejorPrecio = precioConDescuento;
            }
        }
        return mejorPrecio;
    }

    private boolean aplicaPromocion(Promocion promocion, Long productoId, Long sucursalId) {
        boolean productoCoincide = promocion.getProducto() == null
                || promocion.getProducto().getId().equals(productoId);
        boolean sucursalCoincide = promocion.getSucursal() == null
                || promocion.getSucursal().getId().equals(sucursalId);
        return productoCoincide && sucursalCoincide;
    }

    private void sanitizeForInput(Promocion promocion) {
        promocion.setNombre(inputSanitizer.sanitize(promocion.getNombre()));
        promocion.setDescripcion(inputSanitizer.sanitize(promocion.getDescripcion()));
    }

    private void sanitizeForOutput(Promocion promocion) {
        sanitizeForInput(promocion);
    }
}
