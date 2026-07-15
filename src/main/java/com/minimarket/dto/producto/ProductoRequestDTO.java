package com.minimarket.dto.producto;

import com.minimarket.dto.common.IdRefDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Datos para crear o actualizar un producto del catálogo",
        example = """
                {
                  "nombre": "Arroz grano largo 1kg",
                  "precio": 1890,
                  "stock": 10,
                  "descripcion": "Arroz de consumo diario",
                  "categoria": { "id": 1 }
                }
                """)
public class ProductoRequestDTO {

    @Schema(description = "ID del producto (solo en actualización)", example = "1")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    @Schema(description = "Nombre comercial del producto", example = "Arroz grano largo 1kg")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    @Schema(description = "Precio unitario en pesos chilenos", example = "1890")
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Unidades disponibles en inventario", example = "10")
    private Integer stock;

    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
    @Schema(description = "Descripción del producto", example = "Arroz de consumo diario")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "Categoría a la que pertenece el producto")
    private IdRefDTO categoria;

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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public IdRefDTO getCategoria() {
        return categoria;
    }

    public void setCategoria(IdRefDTO categoria) {
        this.categoria = categoria;
    }
}
