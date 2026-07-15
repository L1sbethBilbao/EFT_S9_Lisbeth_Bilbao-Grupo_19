package com.minimarket.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Referencia por ID a otra entidad", example = "{\"id\":1}")
public class IdRefDTO {

    @NotNull(message = "El id de referencia es obligatorio")
    @Schema(description = "Identificador de la entidad referenciada", example = "1")
    private Long id;

    public IdRefDTO() {
    }

    public IdRefDTO(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
