package com.minimarket.dto.pedido;

import com.minimarket.dto.sucursal.SucursalResponseDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;

import java.util.Date;
import java.util.List;

public class PedidoResponseDTO {

    private Long id;
    private UsuarioResponseDTO usuario;
    private SucursalResponseDTO sucursal;
    private Date fecha;
    private String estado;
    private String tipoEntrega;
    private String direccionEntrega;
    private Double total;
    private List<DetallePedidoResponseDTO> detalles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
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

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<DetallePedidoResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
