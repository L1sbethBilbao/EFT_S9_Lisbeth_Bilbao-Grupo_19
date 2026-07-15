package com.minimarket.dto.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class VentaRegistroDTO {

    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @NotEmpty(message = "La venta debe incluir al menos un ítem")
    @Valid
    private List<VentaItemDTO> items;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<VentaItemDTO> getItems() {
        return items;
    }

    public void setItems(List<VentaItemDTO> items) {
        this.items = items;
    }
}
