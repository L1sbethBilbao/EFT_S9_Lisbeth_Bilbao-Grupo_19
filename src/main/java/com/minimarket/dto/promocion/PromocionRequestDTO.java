package com.minimarket.dto.promocion;

import com.minimarket.dto.common.IdRefDTO;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Date;

public class PromocionRequestDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El descuento debe ser mayor a cero")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar 100%")
    private Double descuentoPorcentaje;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private Date fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private Date fechaFin;

    private Boolean activa = true;

    private IdRefDTO producto;

    private IdRefDTO sucursal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(Double descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
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
}
