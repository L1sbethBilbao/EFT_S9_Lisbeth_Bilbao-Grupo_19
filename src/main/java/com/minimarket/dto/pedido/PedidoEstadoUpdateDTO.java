package com.minimarket.dto.pedido;

import jakarta.validation.constraints.NotBlank;

public class PedidoEstadoUpdateDTO {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
