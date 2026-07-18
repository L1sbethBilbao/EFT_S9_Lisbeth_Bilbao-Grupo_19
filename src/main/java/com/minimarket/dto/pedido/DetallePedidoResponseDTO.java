package com.minimarket.dto.pedido;

import com.minimarket.dto.producto.ProductoResponseDTO;

public class DetallePedidoResponseDTO {

    private Long id;
    private ProductoResponseDTO producto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;

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

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
