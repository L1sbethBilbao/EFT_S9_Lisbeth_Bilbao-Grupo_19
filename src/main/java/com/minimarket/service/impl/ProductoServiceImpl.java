package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import com.minimarket.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll().stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    @Override
    public Page<Producto> findAll(Pageable pageable) {
        return productoRepository.findAll(pageable).map(producto -> {
            sanitizeForOutput(producto);
            return producto;
        });
    }

    @Override
    public Producto findById(Long id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto != null) {
            sanitizeForOutput(producto);
        }
        return producto;
    }

    @Override
    public Producto save(Producto producto) {
        sanitizeForInput(producto);
        Producto saved = productoRepository.save(producto);
        sanitizeForOutput(saved);
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    private void sanitizeForInput(Producto producto) {
        producto.setNombre(inputSanitizer.sanitize(producto.getNombre()));
        producto.setDescripcion(inputSanitizer.sanitize(producto.getDescripcion()));
    }

    private void sanitizeForOutput(Producto producto) {
        producto.setNombre(inputSanitizer.sanitize(producto.getNombre()));
        producto.setDescripcion(inputSanitizer.sanitize(producto.getDescripcion()));
    }
}
