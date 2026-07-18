package com.minimarket.controller;

import com.minimarket.dto.ordencompra.OrdenCompraResponseDTO;
import com.minimarket.hateoas.OrdenCompraModelAssembler;
import com.minimarket.mapper.OrdenCompraMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.OrdenCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Órdenes de Compra", description = "Reposición automática y recepción de mercadería")
@RestController
@RequestMapping("/api/ordenes-compra")
@PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
public class OrdenCompraController {

    @Autowired
    private OrdenCompraService ordenCompraService;

    @Autowired
    private OrdenCompraMapper ordenCompraMapper;

    @Autowired
    private OrdenCompraModelAssembler ordenCompraModelAssembler;

    @Operation(summary = "Listar órdenes de compra", description = "Paginado con HATEOAS")
    @GetMapping
    public PagedModel<EntityModel<OrdenCompraResponseDTO>> listarOrdenesCompra(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<OrdenCompraResponseDTO> pagedAssembler) {
        Page<OrdenCompraResponseDTO> page = ordenCompraService.findAll(pageable).map(ordenCompraMapper::toResponse);
        return pagedAssembler.toModel(page, ordenCompraModelAssembler);
    }

    @Operation(summary = "Obtener orden de compra por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<OrdenCompraResponseDTO>> obtenerOrdenCompraPorId(
            @Parameter(description = "ID de la orden", example = "1") @PathVariable Long id) {
        var orden = ordenCompraService.findById(id);
        return (orden != null)
                ? ResponseEntity.ok(ordenCompraModelAssembler.toModel(ordenCompraMapper.toResponse(orden)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Generar órdenes automáticas",
            description = "Crea órdenes para productos con stock <= mínimo en cada sucursal. Cada ítem responde HAL con _links.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Órdenes generadas (lista de EntityModel HAL)"))
    @PostMapping("/generar-automaticas")
    public List<EntityModel<OrdenCompraResponseDTO>> generarOrdenesAutomaticas() {
        return ordenCompraService.generarOrdenesAutomaticas().stream()
                .map(ordenCompraMapper::toResponse)
                .map(ordenCompraModelAssembler::toModel)
                .toList();
    }

    @Operation(summary = "Confirmar recepción",
            description = "Incrementa stock en sucursal y marca orden como RECIBIDA. Respuesta HAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recepción confirmada (HAL)"),
            @ApiResponse(responseCode = "400", description = "Orden ya recibida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada", content = @Content)
    })
    @PostMapping("/{id}/confirmar-recepcion")
    public ResponseEntity<EntityModel<OrdenCompraResponseDTO>> confirmarRecepcion(@PathVariable Long id) {
        try {
            OrdenCompraResponseDTO response = ordenCompraMapper.toResponse(
                    ordenCompraService.confirmarRecepcion(id));
            return ResponseEntity.ok(ordenCompraModelAssembler.toModel(response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
