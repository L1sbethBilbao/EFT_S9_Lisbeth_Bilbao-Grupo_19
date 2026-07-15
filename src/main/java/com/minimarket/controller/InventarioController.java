package com.minimarket.controller;

import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.dto.inventario.InventarioResponseDTO;
import com.minimarket.hateoas.InventarioModelAssembler;
import com.minimarket.mapper.InventarioMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Inventario", description = "Movimientos de stock (entrada/salida). Roles EMPLEADO y GERENTE")
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioMapper inventarioMapper;

    @Autowired
    private InventarioModelAssembler inventarioModelAssembler;

    @Operation(summary = "Listar movimientos de inventario",
            description = "Lista paginada HAL con _embedded, page y links de paginación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de movimientos"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere EMPLEADO o GERENTE)", content = @Content)
    })
    @GetMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public PagedModel<EntityModel<InventarioResponseDTO>> listarMovimientosDeInventario(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<InventarioResponseDTO> pagedAssembler) {
        Page<InventarioResponseDTO> page = inventarioService.findAll(pageable).map(inventarioMapper::toResponse);
        return pagedAssembler.toModel(page, inventarioModelAssembler);
    }

    @Operation(summary = "Obtener movimiento por ID",
            description = "Respuesta HAL con _links (self, inventario, producto).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<EntityModel<InventarioResponseDTO>> obtenerMovimientoPorId(
            @Parameter(description = "ID del movimiento", example = "1") @PathVariable Long id) {
        var inventario = inventarioService.findById(id);
        return (inventario != null)
                ? ResponseEntity.ok(inventarioModelAssembler.toModel(inventarioMapper.toResponse(inventario)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Registrar movimiento de inventario",
            description = "Ejemplo: tipoMovimiento Entrada o Salida. Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public EntityModel<InventarioResponseDTO> registrarMovimiento(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Movimiento de inventario a registrar",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = InventarioRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Entrada de stock",
                                    value = """
                                            {
                                              "producto": { "id": 1 },
                                              "cantidad": 10,
                                              "tipoMovimiento": "Entrada"
                                            }
                                            """)))
            @Valid @RequestBody InventarioRequestDTO inventarioDto) {
        InventarioResponseDTO response = inventarioMapper.toResponse(
                inventarioService.save(inventarioMapper.toEntity(inventarioDto)));
        return inventarioModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar movimiento de inventario",
            description = "Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<EntityModel<InventarioResponseDTO>> actualizarMovimiento(
            @Parameter(description = "ID del movimiento", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del movimiento",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = InventarioRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Actualizar cantidad",
                                    value = """
                                            {
                                              "producto": { "id": 1 },
                                              "cantidad": 5,
                                              "tipoMovimiento": "Salida"
                                            }
                                            """)))
            @Valid @RequestBody InventarioRequestDTO inventarioDto) {
        var existente = inventarioService.findById(id);
        if (existente != null) {
            inventarioDto.setId(id);
            InventarioResponseDTO response = inventarioMapper.toResponse(
                    inventarioService.save(inventarioMapper.toEntity(inventarioDto)));
            return ResponseEntity.ok(inventarioModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar movimiento", description = "Solo rol GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movimiento eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(description = "ID del movimiento", example = "1") @PathVariable Long id) {
        var inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
