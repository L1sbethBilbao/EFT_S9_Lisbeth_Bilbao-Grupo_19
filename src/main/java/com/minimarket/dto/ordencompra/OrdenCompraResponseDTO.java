package com.minimarket.dto.ordencompra;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;

import java.util.Date;
import java.util.List;

public class OrdenCompraResponseDTO {

    private Long id;
    private String proveedor;
    private SucursalResponseDTO sucursal;
    private Date fecha;
    private String estado;
    private List<DetalleOrdenCompraResponseDTO> detalles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public SucursalResponseDTO getSucursal() {
        return sucursal;
    }

    public void setSucursal(SucursalResponseDTO sucursal) {
        this.sucursal = sucursal;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetalleOrdenCompraResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleOrdenCompraResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
