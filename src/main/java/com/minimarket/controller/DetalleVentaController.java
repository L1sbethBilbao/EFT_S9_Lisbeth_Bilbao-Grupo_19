package com.minimarket.controller;

import com.minimarket.dto.detalleventa.DetalleVentaRequestDTO;
import com.minimarket.dto.detalleventa.DetalleVentaResponseDTO;
import com.minimarket.mapper.DetalleVentaMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.DetalleVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Detalle de ventas", description = "Líneas de detalle asociadas a ventas")
@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DetalleVentaMapper detalleVentaMapper;

    @Operation(summary = "Listar detalles de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de detalles"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<DetalleVentaResponseDTO> listarDetalleVentas() {
        return detalleVentaMapper.toResponseList(detalleVentaService.findAll());
    }

    @Operation(summary = "Obtener detalle de venta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<DetalleVentaResponseDTO> obtenerDetalleVentaPorId(
            @Parameter(description = "ID del detalle", example = "1") @PathVariable Long id) {
        var detalleVenta = detalleVentaService.findById(id);
        return (detalleVenta != null) ? ResponseEntity.ok(detalleVentaMapper.toResponse(detalleVenta))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear detalle de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public DetalleVentaResponseDTO guardarDetalleVenta(@Valid @RequestBody DetalleVentaRequestDTO detalleVentaDto) {
        return detalleVentaMapper.toResponse(
                detalleVentaService.save(detalleVentaMapper.toEntity(detalleVentaDto)));
    }

    @Operation(summary = "Actualizar detalle de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere EMPLEADO o GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<DetalleVentaResponseDTO> actualizarDetalleVenta(
            @Parameter(description = "ID del detalle", example = "1") @PathVariable Long id,
            @Valid @RequestBody DetalleVentaRequestDTO detalleVentaDto) {
        var existente = detalleVentaService.findById(id);
        if (existente != null) {
            detalleVentaDto.setId(id);
            return ResponseEntity.ok(detalleVentaMapper.toResponse(
                    detalleVentaService.save(detalleVentaMapper.toEntity(detalleVentaDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar detalle de venta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Detalle eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere EMPLEADO o GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<Void> eliminarDetalleVenta(
            @Parameter(description = "ID del detalle", example = "1") @PathVariable Long id) {
        var detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
