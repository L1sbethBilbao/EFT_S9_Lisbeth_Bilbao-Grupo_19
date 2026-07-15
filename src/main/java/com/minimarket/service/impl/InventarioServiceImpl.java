package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.util.InputSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    private static final String TIPO_ENTRADA = "Entrada";
    private static final String TIPO_SALIDA = "Salida";

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll().stream().peek(this::sanitizeForOutput).toList();
    }

    @Override
    public Page<Inventario> findAll(Pageable pageable) {
        return inventarioRepository.findAll(pageable).map(inventario -> {
            sanitizeForOutput(inventario);
            return inventario;
        });
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id)
                .map(inventario -> {
                    sanitizeForOutput(inventario);
                    return inventario;
                })
                .orElse(null);
    }

    @Override
    public Inventario save(Inventario inventario) {
        return registrarMovimiento(inventario);
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId).stream()
                .peek(this::sanitizeForOutput)
                .toList();
    }

    @Override
    @Transactional
    public Inventario registrarMovimiento(Inventario inventario) {
        sanitizeForInput(inventario);
        validarCamposMovimiento(inventario);

        Long productoId = inventario.getProducto().getId();
        Producto producto = validarProductoAsociado(productoId);
        inventario.setProducto(producto);

        aplicarMovimientoStock(producto, inventario.getTipoMovimiento(), inventario.getCantidad());
        productoRepository.save(producto);

        if (inventario.getFechaMovimiento() == null) {
            inventario.setFechaMovimiento(new Date());
        }

        Inventario saved = inventarioRepository.save(inventario);
        sanitizeForOutput(saved);
        return saved;
    }

    @Override
    public void validarCamposMovimiento(Inventario inventario) {
        if (inventario.getCantidad() == null || inventario.getCantidad() <= 0) {
            throw new IllegalArgumentException(BusinessErrorMessages.CANTIDAD_OBLIGATORIA);
        }
        if (inventario.getTipoMovimiento() == null || inventario.getTipoMovimiento().isBlank()) {
            throw new IllegalArgumentException(BusinessErrorMessages.TIPO_MOVIMIENTO_OBLIGATORIO);
        }
    }

    @Override
    public Producto validarProductoAsociado(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.PRODUCTO_NO_ENCONTRADO, productoId)));
    }

    private void aplicarMovimientoStock(Producto producto, String tipoMovimiento, int cantidad) {
        String tipo = tipoMovimiento.trim();
        if (TIPO_ENTRADA.equalsIgnoreCase(tipo)) {
            int stockActual = producto.getStock() != null ? producto.getStock() : 0;
            producto.setStock(stockActual + cantidad);
            return;
        }
        if (TIPO_SALIDA.equalsIgnoreCase(tipo)) {
            if (producto.getStock() == null || producto.getStock() < cantidad) {
                throw new StockInsuficienteException(
                        String.format(BusinessErrorMessages.STOCK_INSUFICIENTE,
                                producto.getNombre(), producto.getStock(), cantidad));
            }
            producto.setStock(producto.getStock() - cantidad);
            return;
        }
        throw new IllegalArgumentException(
                String.format(BusinessErrorMessages.TIPO_MOVIMIENTO_INVALIDO, tipoMovimiento));
    }

    private void sanitizeForInput(Inventario inventario) {
        if (inventario.getTipoMovimiento() != null) {
            inventario.setTipoMovimiento(inputSanitizer.sanitize(inventario.getTipoMovimiento()));
        }
    }

    private void sanitizeForOutput(Inventario inventario) {
        sanitizeForInput(inventario);
    }
}
