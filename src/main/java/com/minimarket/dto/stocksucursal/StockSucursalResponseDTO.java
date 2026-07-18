package com.minimarket.dto.stocksucursal;

import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;

public class StockSucursalResponseDTO {

    private Long id;
    private ProductoResponseDTO producto;
    private SucursalResponseDTO sucursal;
    private Integer cantidad;
    private Integer stockMinimo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductoResponseDTO getProducto() {
        return producto;
    }

    public void setProducto(ProductoResponseDTO producto) {
        this.producto = producto;
    }

    public SucursalResponseDTO getSucursal() {
        return sucursal;
    }

    public void setSucursal(SucursalResponseDTO sucursal) {
        this.sucursal = sucursal;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}
