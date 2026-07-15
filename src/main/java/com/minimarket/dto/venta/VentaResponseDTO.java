package com.minimarket.dto.venta;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;

import java.util.Date;

public class VentaResponseDTO {

    private Long id;
    private UsuarioResponseDTO usuario;
    private Date fecha;
    private Double total;

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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
