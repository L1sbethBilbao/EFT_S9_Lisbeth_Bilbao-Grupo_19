package com.minimarket.dto.detalleventa;

import com.minimarket.dto.common.IdRefDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DetalleVentaRequestDTO {

    private Long id;

    @NotNull(message = "La venta es obligatoria")
    @Valid
    private IdRefDTO venta;

    @NotNull(message = "El producto es obligatorio")
    @Valid
    private IdRefDTO producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
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

    public IdRefDTO getProducto() {
        return producto;
    }

    public void setProducto(IdRefDTO producto) {
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
