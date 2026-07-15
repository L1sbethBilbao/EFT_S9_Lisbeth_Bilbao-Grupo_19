package com.minimarket.dto.inventario;

import com.minimarket.dto.common.IdRefDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

public class InventarioRequestDTO {

    @Schema(description = "ID del movimiento (solo en actualización)", example = "1")
    private Long id;

    @Schema(description = "Producto asociado al movimiento", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El producto es obligatorio")
    @Valid
    private IdRefDTO producto;

    @Schema(description = "Cantidad del movimiento", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Schema(description = "Tipo de movimiento: Entrada o Salida", example = "Entrada", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Size(max = 50, message = "El tipo de movimiento no puede superar 50 caracteres")
    private String tipoMovimiento;

    @Schema(description = "Fecha del movimiento (opcional, se asigna automáticamente si no se envía)")
    private Date fechaMovimiento;

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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }
}
