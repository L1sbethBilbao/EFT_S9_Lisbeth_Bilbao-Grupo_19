package com.minimarket.service.impl;

import com.minimarket.entity.Sucursal;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.SucursalService;
import com.minimarket.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SucursalServiceImpl implements SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public List<Sucursal> findAll() {
        return sucursalRepository.findAll().stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    @Override
    public Page<Sucursal> findAll(Pageable pageable) {
        return sucursalRepository.findAll(pageable).map(sucursal -> {
            sanitizeForOutput(sucursal);
            return sucursal;
        });
    }

    @Override
    public Sucursal findById(Long id) {
        Sucursal sucursal = sucursalRepository.findById(id).orElse(null);
        if (sucursal != null) {
            sanitizeForOutput(sucursal);
        }
        return sucursal;
    }

    @Override
    public Sucursal save(Sucursal sucursal) {
        sanitizeForInput(sucursal);
        Sucursal saved = sucursalRepository.save(sucursal);
        sanitizeForOutput(saved);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        sucursalRepository.deleteById(id);
    }

    private void sanitizeForInput(Sucursal sucursal) {
        sucursal.setNombre(inputSanitizer.sanitize(sucursal.getNombre()));
        sucursal.setDireccion(inputSanitizer.sanitize(sucursal.getDireccion()));
        sucursal.setComuna(inputSanitizer.sanitize(sucursal.getComuna()));
    }

    private void sanitizeForOutput(Sucursal sucursal) {
        sanitizeForInput(sucursal);
    }
}
