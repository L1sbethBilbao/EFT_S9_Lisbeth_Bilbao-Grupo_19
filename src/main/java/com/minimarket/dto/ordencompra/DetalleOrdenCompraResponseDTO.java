package com.minimarket.dto.ordencompra;

import com.minimarket.dto.producto.ProductoResponseDTO;

public class DetalleOrdenCompraResponseDTO {

    private Long id;
    private ProductoResponseDTO producto;
    private Integer cantidad;
    private Double costoUnitario;

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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(Double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
