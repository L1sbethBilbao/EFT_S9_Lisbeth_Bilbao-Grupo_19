package com.minimarket.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PedidoRegistroDTO {

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;

    @NotBlank(message = "El tipo de entrega es obligatorio")
    private String tipoEntrega;

    private String direccionEntrega;

    @NotEmpty(message = "El pedido debe incluir al menos un ítem")
    @Valid
    private List<PedidoItemDTO> items;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
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

    public List<PedidoItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemDTO> items) {
        this.items = items;
    }
}
