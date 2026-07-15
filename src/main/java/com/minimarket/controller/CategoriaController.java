package com.minimarket.controller;

import com.minimarket.dto.categoria.CategoriaRequestDTO;
import com.minimarket.dto.categoria.CategoriaResponseDTO;
import com.minimarket.mapper.CategoriaMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categorías", description = "Clasificación de productos del catálogo")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaMapper categoriaMapper;

    @Operation(summary = "Listar categorías", description = "Consulta pública de categorías")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de categorías"))
    @SecurityRequirements
    @GetMapping
    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaMapper.toResponseList(categoriaService.findAll());
    }

    @Operation(summary = "Obtener categoría por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerCategoriaPorId(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        var categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(categoriaMapper.toResponse(categoria))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear categoría", description = "Solo rol GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public CategoriaResponseDTO guardarCategoria(@Valid @RequestBody CategoriaRequestDTO categoriaDto) {
        return categoriaMapper.toResponse(categoriaService.save(categoriaMapper.toEntity(categoriaDto)));
    }

    @Operation(summary = "Actualizar categoría", description = "Solo rol GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO categoriaDto) {
        var categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            categoriaDto.setId(id);
            return ResponseEntity.ok(categoriaMapper.toResponse(
                    categoriaService.save(categoriaMapper.toEntity(categoriaDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar categoría", description = "Solo rol GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        var categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
