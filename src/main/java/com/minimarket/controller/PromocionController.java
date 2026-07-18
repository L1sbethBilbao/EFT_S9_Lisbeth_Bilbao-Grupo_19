package com.minimarket.controller;

import com.minimarket.dto.promocion.PromocionRequestDTO;
import com.minimarket.dto.promocion.PromocionResponseDTO;
import com.minimarket.hateoas.PromocionModelAssembler;
import com.minimarket.mapper.PromocionMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Promociones", description = "Ofertas y promociones centralizadas")
@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    @Autowired
    private PromocionMapper promocionMapper;

    @Autowired
    private PromocionModelAssembler promocionModelAssembler;

    @Operation(summary = "Listar promociones", description = "Consulta pública paginada con HATEOAS")
    @SecurityRequirements
    @GetMapping
    public PagedModel<EntityModel<PromocionResponseDTO>> listarPromociones(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<PromocionResponseDTO> pagedAssembler) {
        Page<PromocionResponseDTO> page = promocionService.findAll(pageable).map(promocionMapper::toResponse);
        return pagedAssembler.toModel(page, promocionModelAssembler);
    }

    @Operation(summary = "Listar promociones activas", description = "Consulta pública de promociones vigentes")
    @SecurityRequirements
    @GetMapping("/activas")
    public List<PromocionResponseDTO> listarPromocionesActivas() {
        return promocionMapper.toResponseList(promocionService.findActivas());
    }

    @Operation(summary = "Obtener promoción por ID")
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PromocionResponseDTO>> obtenerPromocionPorId(
            @Parameter(description = "ID de la promoción", example = "1") @PathVariable Long id) {
        var promocion = promocionService.findById(id);
        return (promocion != null)
                ? ResponseEntity.ok(promocionModelAssembler.toModel(promocionMapper.toResponse(promocion)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear promoción", description = "Solo rol GERENTE. Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promoción creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public EntityModel<PromocionResponseDTO> guardarPromocion(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Promoción centralizada",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PromocionRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Promo 15% producto en sucursal",
                                    value = """
                                            {
                                              "nombre": "Promo EFT S9",
                                              "descripcion": "Descuento de evidencia",
                                              "descuentoPorcentaje": 15,
                                              "fechaInicio": "2026-01-01T00:00:00.000Z",
                                              "fechaFin": "2026-12-31T23:59:59.000Z",
                                              "activa": true,
                                              "producto": { "id": 1 },
                                              "sucursal": { "id": 1 }
                                            }
                                            """)))
            @Valid @RequestBody PromocionRequestDTO dto) {
        PromocionResponseDTO response = promocionMapper.toResponse(
                promocionService.save(promocionMapper.toEntity(dto)));
        return promocionModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar promoción", description = "Solo rol GERENTE")
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<EntityModel<PromocionResponseDTO>> actualizarPromocion(
            @PathVariable Long id,
            @Valid @RequestBody PromocionRequestDTO dto) {
        var existente = promocionService.findById(id);
        if (existente != null) {
            dto.setId(id);
            PromocionResponseDTO response = promocionMapper.toResponse(
                    promocionService.save(promocionMapper.toEntity(dto)));
            return ResponseEntity.ok(promocionModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar promoción", description = "Solo rol GERENTE")
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarPromocion(@PathVariable Long id) {
        var promocion = promocionService.findById(id);
        if (promocion != null) {
            promocionService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
