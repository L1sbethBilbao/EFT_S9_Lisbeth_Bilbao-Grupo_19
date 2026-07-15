package com.minimarket.controller;

import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.dto.venta.VentaRequestDTO;
import com.minimarket.dto.venta.VentaResponseDTO;
import com.minimarket.mapper.VentaMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ventas", description = "Registro y consulta de ventas del minimarket")
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaMapper ventaMapper;

    @Operation(summary = "Listar ventas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<VentaResponseDTO> listarVentas() {
        return ventaMapper.toResponseList(ventaService.findAll());
    }

    @Operation(summary = "Obtener venta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public VentaResponseDTO obtenerVentaPorId(
            @Parameter(description = "ID de la venta", example = "1") @PathVariable Long id) {
        var venta = ventaService.findById(id);
        return venta != null ? ventaMapper.toResponse(venta) : null;
    }

    @Operation(summary = "Registrar venta con items",
            description = "Descuenta stock y calcula total. Roles EMPLEADO y GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere EMPLEADO o GERENTE)", content = @Content)
    })
    @PostMapping("/registrar")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public VentaResponseDTO registrarVenta(@Valid @RequestBody VentaRegistroDTO dto) {
        return ventaMapper.toResponse(ventaService.registrarVenta(dto));
    }

    @Operation(summary = "Guardar venta (CRUD directo)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta guardada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public VentaResponseDTO guardarVenta(@Valid @RequestBody VentaRequestDTO ventaDto) {
        return ventaMapper.toResponse(ventaService.save(ventaMapper.toEntity(ventaDto)));
    }
}
