package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.dto.stocksucursal.DisponibilidadResponseDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.StockSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockSucursalServiceImpl implements StockSucursalService {

    @Autowired
    private StockSucursalRepository stockSucursalRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<StockSucursal> findAll() {
        return stockSucursalRepository.findAll();
    }

    @Override
    public Page<StockSucursal> findAll(Pageable pageable) {
        return stockSucursalRepository.findAll(pageable);
    }

    @Override
    public StockSucursal findById(Long id) {
        return stockSucursalRepository.findById(id).orElse(null);
    }

    @Override
    public StockSucursal save(StockSucursal stockSucursal) {
        return stockSucursalRepository.save(stockSucursal);
    }

    @Override
    public void deleteById(Long id) {
        stockSucursalRepository.deleteById(id);
    }

    @Override
    public List<StockSucursal> findBySucursalId(Long sucursalId) {
        validarSucursalExiste(sucursalId);
        return stockSucursalRepository.findBySucursalId(sucursalId);
    }

    @Override
    public List<DisponibilidadResponseDTO> consultarDisponibilidad(Long sucursalId) {
        validarSucursalExiste(sucursalId);
        return stockSucursalRepository.findBySucursalId(sucursalId).stream()
                .map(this::toDisponibilidad)
                .toList();
    }

    @Override
    public void validarStockDisponible(Long sucursalId, Long productoId, int cantidad) {
        StockSucursal stock = obtenerStock(sucursalId, productoId);
        if (stock.getCantidad() == null || stock.getCantidad() < cantidad) {
            String nombreProducto = stock.getProducto() != null ? stock.getProducto().getNombre() : "producto";
            throw new StockInsuficienteException(
                    String.format(BusinessErrorMessages.STOCK_INSUFICIENTE,
                            nombreProducto, stock.getCantidad(), cantidad));
        }
    }

    @Override
    @Transactional
    public void decrementarStock(Long sucursalId, Long productoId, int cantidad) {
        validarStockDisponible(sucursalId, productoId, cantidad);
        StockSucursal stock = obtenerStock(sucursalId, productoId);
        stock.setCantidad(stock.getCantidad() - cantidad);
        stockSucursalRepository.save(stock);
    }

    @Override
    @Transactional
    public void incrementarStock(Long sucursalId, Long productoId, int cantidad) {
        StockSucursal stock = obtenerStock(sucursalId, productoId);
        int actual = stock.getCantidad() != null ? stock.getCantidad() : 0;
        stock.setCantidad(actual + cantidad);
        stockSucursalRepository.save(stock);
    }

    private StockSucursal obtenerStock(Long sucursalId, Long productoId) {
        return stockSucursalRepository.findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.STOCK_SUCURSAL_NO_ENCONTRADO,
                                productoId, sucursalId)));
    }

    private void validarSucursalExiste(Long sucursalId) {
        if (!sucursalRepository.existsById(sucursalId)) {
            throw new IllegalArgumentException(
                    String.format(BusinessErrorMessages.SUCURSAL_NO_ENCONTRADA, sucursalId));
        }
    }

    private DisponibilidadResponseDTO toDisponibilidad(StockSucursal stock) {
        Producto producto = stock.getProducto();
        DisponibilidadResponseDTO dto = new DisponibilidadResponseDTO();
        dto.setProductoId(producto.getId());
        dto.setProductoNombre(producto.getNombre());
        dto.setPrecio(producto.getPrecio());
        dto.setCantidadDisponible(stock.getCantidad());
        dto.setDisponible(stock.getCantidad() != null && stock.getCantidad() > 0);
        return dto;
    }
}
