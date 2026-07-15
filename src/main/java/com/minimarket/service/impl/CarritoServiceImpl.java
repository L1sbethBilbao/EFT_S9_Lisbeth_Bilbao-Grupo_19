package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.CarritoService;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Page<Carrito> findAll(Pageable pageable) {
        return carritoRepository.findAll(pageable);
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(id).orElse(null);
    }

    @Override
    public Carrito save(Carrito carrito) {
        return carritoRepository.save(carrito);
    }

    @Override
    public void deleteById(Long id) {
        carritoRepository.deleteById(id);
    }

    @Override
    public List<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Carrito agregarProducto(Long usuarioId, Long productoId, int cantidad) {
        Usuario usuario = validarUsuarioCarrito(usuarioId);
        validarStockDisponible(productoId, cantidad);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.PRODUCTO_NO_ENCONTRADO, productoId)));

        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(cantidad);
        return carritoRepository.save(carrito);
    }

    @Override
    public Usuario validarUsuarioCarrito(Long usuarioId) {
        return usuarioService.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.USUARIO_NO_ENCONTRADO, usuarioId)));
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
