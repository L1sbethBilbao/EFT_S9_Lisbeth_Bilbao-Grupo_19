package com.minimarket.service.impl;

import com.minimarket.dto.venta.VentaItemDTO;
import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Page<Venta> findAll(Pageable pageable) {
        return ventaRepository.findAll(pageable);
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        if (venta.getTotal() == null) {
            venta.setTotal(0.0);
        }
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public Venta registrarVenta(VentaRegistroDTO dto) {
        Usuario usuario = usuarioService.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.USUARIO_NO_ENCONTRADO, dto.getUsuarioId())));

        usuarioService.validarDatosCompletos(usuario);
        usuarioService.validarPuedeRegistrarVenta(usuario);

        List<DetalleVenta> detalles = new ArrayList<>();
        for (VentaItemDTO item : dto.getItems()) {
            validarStockDisponible(item.getProductoId(), item.getCantidad());
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(BusinessErrorMessages.PRODUCTO_NO_ENCONTRADO, item.getProductoId())));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalles.add(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setTotal(calcularTotal(detalles));
        venta.setDetalles(detalles);
        detalles.forEach(d -> d.setVenta(venta));

        return ventaRepository.save(venta);
    }

    @Override
    public double calcularTotal(List<DetalleVenta> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        return detalles.stream()
                .mapToDouble(d -> d.getPrecio() * d.getCantidad())
                .sum();
    }

    @Override
    public void validarStockDisponible(Long productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.PRODUCTO_NO_ENCONTRADO, productoId)));
        if (producto.getStock() == null || producto.getStock() < cantidad) {
            throw new StockInsuficienteException(
                    String.format(BusinessErrorMessages.STOCK_INSUFICIENTE,
                            producto.getNombre(), producto.getStock(), cantidad));
        }
    }
}
