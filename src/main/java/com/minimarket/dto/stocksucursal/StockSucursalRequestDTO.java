package com.minimarket.dto.stocksucursal;

import com.minimarket.dto.common.IdRefDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockSucursalRequestDTO {

    private Long id;

    @NotNull(message = "El producto es obligatorio")
    private IdRefDTO producto;

    @NotNull(message = "La sucursal es obligatoria")
    private IdRefDTO sucursal;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidad;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdRefDTO getProducto() {
        return producto;
    }

    public void setProducto(IdRefDTO producto) {
        this.producto = producto;
    }

    public IdRefDTO getSucursal() {
        return sucursal;
    }

    public void setSucursal(IdRefDTO sucursal) {
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
