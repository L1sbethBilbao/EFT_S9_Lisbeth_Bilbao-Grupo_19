package com.minimarket.dto.detalleventa;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;

public class DetalleVentaResponseDTO {

    private Long id;
    private IdRefDTO venta;
    private ProductoResponseDTO producto;
    private Integer cantidad;
    private Double precio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdRefDTO getVenta() {
        return venta;
    }

    public void setVenta(IdRefDTO venta) {
        this.venta = venta;
    }

    public ProductoResponseDTO getProducto() {
        return producto;
    }

    public void setProducto(ProductoResponseDTO producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }
}
