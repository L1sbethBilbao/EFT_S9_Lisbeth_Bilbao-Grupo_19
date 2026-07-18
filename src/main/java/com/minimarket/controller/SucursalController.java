package com.minimarket.controller;

import com.minimarket.dto.sucursal.SucursalRequestDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;
import com.minimarket.hateoas.SucursalModelAssembler;
import com.minimarket.mapper.SucursalMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.SucursalService;
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

@Tag(name = "Sucursales", description = "Gestión de sucursales MiniMarket Plus")
@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {

    @Autowired
    private SucursalService sucursalService;

    @Autowired
    private SucursalMapper sucursalMapper;

    @Autowired
    private SucursalModelAssembler sucursalModelAssembler;

    @Operation(summary = "Listar sucursales", description = "Consulta pública paginada con HATEOAS")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Página de sucursales"))
    @SecurityRequirements
    @GetMapping
    public PagedModel<EntityModel<SucursalResponseDTO>> listarSucursales(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<SucursalResponseDTO> pagedAssembler) {
        Page<SucursalResponseDTO> page = sucursalService.findAll(pageable).map(sucursalMapper::toResponse);
        return pagedAssembler.toModel(page, sucursalModelAssembler);
    }

    @Operation(summary = "Obtener sucursal por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucursal encontrada"),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    })
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<SucursalResponseDTO>> obtenerSucursalPorId(
            @Parameter(description = "ID de la sucursal", example = "1") @PathVariable Long id) {
        var sucursal = sucursalService.findById(id);
        return (sucursal != null)
                ? ResponseEntity.ok(sucursalModelAssembler.toModel(sucursalMapper.toResponse(sucursal)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear sucursal", description = "Solo rol GERENTE. Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucursal creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public EntityModel<SucursalResponseDTO> guardarSucursal(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Sucursal a crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SucursalRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Sucursal Providencia",
                                    value = """
                                            {
                                              "nombre": "MiniMarket Plus Las Condes",
                                              "direccion": "Av. Apoquindo 4500",
                                              "comuna": "Las Condes",
                                              "activa": true
                                            }
                                            """)))
            @Valid @RequestBody SucursalRequestDTO dto) {
        SucursalResponseDTO response = sucursalMapper.toResponse(
                sucursalService.save(sucursalMapper.toEntity(dto)));
        return sucursalModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar sucursal", description = "Solo rol GERENTE. Respuesta HAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucursal actualizada"),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<EntityModel<SucursalResponseDTO>> actualizarSucursal(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados de la sucursal",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SucursalRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Actualizar dirección",
                                    value = """
                                            {
                                              "nombre": "MiniMarket Plus Providencia",
                                              "direccion": "Av. Providencia 2120",
                                              "comuna": "Providencia",
                                              "activa": true
                                            }
                                            """)))
            @Valid @RequestBody SucursalRequestDTO dto) {
        var existente = sucursalService.findById(id);
        if (existente != null) {
            dto.setId(id);
            SucursalResponseDTO response = sucursalMapper.toResponse(
                    sucursalService.save(sucursalMapper.toEntity(dto)));
            return ResponseEntity.ok(sucursalModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar sucursal", description = "Solo rol GERENTE")
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarSucursal(@PathVariable Long id) {
        var sucursal = sucursalService.findById(id);
        if (sucursal != null) {
            sucursalService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
