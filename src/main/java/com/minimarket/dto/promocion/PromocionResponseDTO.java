package com.minimarket.dto.promocion;

import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;

import java.util.Date;

public class PromocionResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double descuentoPorcentaje;
    private Date fechaInicio;
    private Date fechaFin;
    private Boolean activa;
    private ProductoResponseDTO producto;
    private SucursalResponseDTO sucursal;

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
}
