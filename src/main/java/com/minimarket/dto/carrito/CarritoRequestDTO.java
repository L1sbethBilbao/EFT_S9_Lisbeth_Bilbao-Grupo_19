package com.minimarket.dto.carrito;

import com.minimarket.dto.common.IdRefDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(
        description = "Datos para agregar o modificar un item en el carrito de compras",
        example = """
                {
                  "usuario": { "id": 1 },
                  "producto": { "id": 1 },
                  "cantidad": 2
                }
                """)
public class CarritoRequestDTO {

    @Schema(description = "ID del item en carrito (solo en actualización)", example = "1")
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @Valid
    @Schema(description = "Usuario propietario del carrito")
    private IdRefDTO usuario;

    @NotNull(message = "El producto es obligatorio")
    @Valid
    @Schema(description = "Producto a agregar al carrito")
    private IdRefDTO producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Schema(description = "Cantidad de unidades", example = "2", minimum = "1")
    private Integer cantidad;

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
}
