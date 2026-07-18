package com.minimarket.controller;

import com.minimarket.dto.stocksucursal.DisponibilidadResponseDTO;
import com.minimarket.dto.stocksucursal.StockSucursalRequestDTO;
import com.minimarket.dto.stocksucursal.StockSucursalResponseDTO;
import com.minimarket.hateoas.StockSucursalModelAssembler;
import com.minimarket.mapper.StockSucursalMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.StockSucursalService;
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

@Tag(name = "Stock Sucursal", description = "Stock por sucursal y disponibilidad de productos")
@RestController
@RequestMapping("/api/stock-sucursal")
public class StockSucursalController {

    @Autowired
    private StockSucursalService stockSucursalService;

    @Autowired
    private StockSucursalMapper stockSucursalMapper;

    @Autowired
    private StockSucursalModelAssembler stockSucursalModelAssembler;

    @Operation(summary = "Listar stock por sucursal", description = "Paginado con HATEOAS. Roles EMPLEADO y GERENTE")
    @GetMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public PagedModel<EntityModel<StockSucursalResponseDTO>> listarStockSucursal(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<StockSucursalResponseDTO> pagedAssembler) {
        Page<StockSucursalResponseDTO> page = stockSucursalService.findAll(pageable).map(stockSucursalMapper::toResponse);
        return pagedAssembler.toModel(page, stockSucursalModelAssembler);
    }

    @Operation(summary = "Consultar disponibilidad por sucursal", description = "Consulta pública")
    @SecurityRequirements
    @GetMapping("/disponibilidad/{sucursalId}")
    public List<DisponibilidadResponseDTO> consultarDisponibilidad(
            @Parameter(description = "ID de la sucursal", example = "1") @PathVariable Long sucursalId) {
        return stockSucursalService.consultarDisponibilidad(sucursalId);
    }

    @Operation(summary = "Obtener stock por ID")
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<EntityModel<StockSucursalResponseDTO>> obtenerStockPorId(@PathVariable Long id) {
        var stock = stockSucursalService.findById(id);
        return (stock != null)
                ? ResponseEntity.ok(stockSucursalModelAssembler.toModel(stockSucursalMapper.toResponse(stock)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear registro de stock", description = "Roles EMPLEADO y GERENTE. Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock creado (HAL)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public EntityModel<StockSucursalResponseDTO> guardarStock(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Stock por sucursal a crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = StockSucursalRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Stock Providencia",
                                    value = """
                                            {
                                              "producto": { "id": 1 },
                                              "sucursal": { "id": 1 },
                                              "cantidad": 40,
                                              "stockMinimo": 10
                                            }
                                            """)))
            @Valid @RequestBody StockSucursalRequestDTO dto) {
        StockSucursalResponseDTO response = stockSucursalMapper.toResponse(
                stockSucursalService.save(stockSucursalMapper.toEntity(dto)));
        return stockSucursalModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar stock", description = "Roles EMPLEADO y GERENTE. Respuesta HAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<EntityModel<StockSucursalResponseDTO>> actualizarStock(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados de stock",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = StockSucursalRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Reponer stock",
                                    value = """
                                            {
                                              "producto": { "id": 1 },
                                              "sucursal": { "id": 1 },
                                              "cantidad": 55,
                                              "stockMinimo": 10
                                            }
                                            """)))
            @Valid @RequestBody StockSucursalRequestDTO dto) {
        var existente = stockSucursalService.findById(id);
        if (existente != null) {
            dto.setId(id);
            StockSucursalResponseDTO response = stockSucursalMapper.toResponse(
                    stockSucursalService.save(stockSucursalMapper.toEntity(dto)));
            return ResponseEntity.ok(stockSucursalModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar registro de stock", description = "Solo rol GERENTE")
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarStock(@PathVariable Long id) {
        var stock = stockSucursalService.findById(id);
        if (stock != null) {
            stockSucursalService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
