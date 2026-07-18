package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.constants.OrdenCompraConstants;
import com.minimarket.entity.DetalleOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.repository.OrdenCompraRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.service.OrdenCompraService;
import com.minimarket.service.StockSucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private static final String PROVEEDOR_DEFAULT = "Distribuidora Central";

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private StockSucursalRepository stockSucursalRepository;

    @Autowired
    private StockSucursalService stockSucursalService;

    @Override
    public List<OrdenCompra> findAll() {
        return ordenCompraRepository.findAll();
    }

    @Override
    public Page<OrdenCompra> findAll(Pageable pageable) {
        return ordenCompraRepository.findAll(pageable);
    }

    @Override
    public OrdenCompra findById(Long id) {
        return ordenCompraRepository.findById(id).orElse(null);
    }

    @Override
    public OrdenCompra save(OrdenCompra ordenCompra) {
        return ordenCompraRepository.save(ordenCompra);
    }

    @Override
    @Transactional
    public List<OrdenCompra> generarOrdenesAutomaticas() {
        List<StockSucursal> stocksBajoMinimo = stockSucursalRepository.findAllBajoMinimo();
        if (stocksBajoMinimo.isEmpty()) {
            return List.of();
        }

        Map<Long, OrdenCompra> ordenesPorSucursal = new LinkedHashMap<>();

        for (StockSucursal stock : stocksBajoMinimo) {
            Long sucursalId = stock.getSucursal().getId();
            OrdenCompra orden = ordenesPorSucursal.computeIfAbsent(sucursalId, id -> {
                OrdenCompra nueva = new OrdenCompra();
                nueva.setProveedor(PROVEEDOR_DEFAULT);
                nueva.setSucursal(stock.getSucursal());
                nueva.setFecha(new Date());
                nueva.setEstado(OrdenCompraConstants.ESTADO_PENDIENTE);
                nueva.setDetalles(new ArrayList<>());
                return nueva;
            });

            int cantidadReponer = Math.max(stock.getStockMinimo() * 2 - stock.getCantidad(), stock.getStockMinimo());

            DetalleOrdenCompra detalle = new DetalleOrdenCompra();
            detalle.setProducto(stock.getProducto());
            detalle.setCantidad(cantidadReponer);
            detalle.setCostoUnitario(stock.getProducto().getPrecio() * 0.7);
            detalle.setOrdenCompra(orden);
            orden.getDetalles().add(detalle);
        }

        List<OrdenCompra> ordenesGuardadas = new ArrayList<>();
        for (OrdenCompra orden : ordenesPorSucursal.values()) {
            ordenesGuardadas.add(ordenCompraRepository.save(orden));
        }
        return ordenesGuardadas;
    }

    @Override
    @Transactional
    public OrdenCompra confirmarRecepcion(Long ordenCompraId) {
        OrdenCompra orden = ordenCompraRepository.findById(ordenCompraId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.ORDEN_COMPRA_NO_ENCONTRADA, ordenCompraId)));

        if (OrdenCompraConstants.ESTADO_RECIBIDA.equals(orden.getEstado())) {
            throw new IllegalStateException(BusinessErrorMessages.ORDEN_COMPRA_YA_RECIBIDA);
        }

        Sucursal sucursal = orden.getSucursal();
        for (DetalleOrdenCompra detalle : orden.getDetalles()) {
            stockSucursalService.incrementarStock(
                    sucursal.getId(),
                    detalle.getProducto().getId(),
                    detalle.getCantidad());
        }

        orden.setEstado(OrdenCompraConstants.ESTADO_RECIBIDA);
        return ordenCompraRepository.save(orden);
    }
}
