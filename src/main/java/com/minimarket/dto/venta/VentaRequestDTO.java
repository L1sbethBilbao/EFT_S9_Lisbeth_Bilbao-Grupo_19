package com.minimarket.dto.venta;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class VentaRequestDTO {

    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @Valid
    private IdRefDTO usuario;

    private Date fecha;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdRefDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(IdRefDTO usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
