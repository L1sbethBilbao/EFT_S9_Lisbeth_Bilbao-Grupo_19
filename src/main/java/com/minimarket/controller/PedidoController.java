package com.minimarket.controller;

import com.minimarket.dto.pedido.PedidoEstadoUpdateDTO;
import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.dto.pedido.PedidoResponseDTO;
import com.minimarket.hateoas.PedidoModelAssembler;
import com.minimarket.mapper.PedidoMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.PedidoService;
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

@Tag(name = "Pedidos", description = "Pedidos en línea para retiro o despacho")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private PedidoModelAssembler pedidoModelAssembler;

    @Operation(summary = "Listar pedidos", description = "Paginado con HATEOAS. Usuario autenticado")
    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public PagedModel<EntityModel<PedidoResponseDTO>> listarPedidos(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<PedidoResponseDTO> pagedAssembler) {
        Page<PedidoResponseDTO> page = pedidoService.findAll(pageable).map(pedidoMapper::toResponse);
        return pagedAssembler.toModel(page, pedidoModelAssembler);
    }

    @Operation(summary = "Obtener pedido por ID")
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<EntityModel<PedidoResponseDTO>> obtenerPedidoPorId(
            @Parameter(description = "ID del pedido", example = "1") @PathVariable Long id) {
        var pedido = pedidoService.findById(id);
        return (pedido != null)
                ? ResponseEntity.ok(pedidoModelAssembler.toModel(pedidoMapper.toResponse(pedido)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Registrar pedido",
            description = "Usuario autenticado. Descuenta stock de sucursal, aplica promociones y responde HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido registrado (HAL)"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @PostMapping("/registrar")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public EntityModel<PedidoResponseDTO> registrarPedido(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Pedido RETIRO o DESPACHO",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PedidoRegistroDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Retiro en tienda",
                                            value = """
                                                    {
                                                      "usuarioId": 1,
                                                      "sucursalId": 1,
                                                      "tipoEntrega": "RETIRO",
                                                      "items": [
                                                        { "productoId": 1, "cantidad": 2 }
                                                      ]
                                                    }
                                                    """),
                                    @ExampleObject(
                                            name = "Despacho a domicilio",
                                            value = """
                                                    {
                                                      "usuarioId": 1,
                                                      "sucursalId": 1,
                                                      "tipoEntrega": "DESPACHO",
                                                      "direccionEntrega": "Av. Providencia 1234, Santiago",
                                                      "items": [
                                                        { "productoId": 1, "cantidad": 1 }
                                                      ]
                                                    }
                                                    """)
                            }))
            @Valid @RequestBody PedidoRegistroDTO dto) {
        PedidoResponseDTO response = pedidoMapper.toResponse(pedidoService.registrarPedido(dto));
        return pedidoModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar estado del pedido", description = "Roles EMPLEADO y GERENTE. Respuesta HAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @PatchMapping("/{id}/estado")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<EntityModel<PedidoResponseDTO>> actualizarEstado(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado del pedido",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PedidoEstadoUpdateDTO.class),
                            examples = @ExampleObject(
                                    name = "Confirmar pedido",
                                    value = "{\"estado\": \"CONFIRMADO\"}")))
            @Valid @RequestBody PedidoEstadoUpdateDTO dto) {
        try {
            PedidoResponseDTO response = pedidoMapper.toResponse(
                    pedidoService.actualizarEstado(id, dto.getEstado()));
            return ResponseEntity.ok(pedidoModelAssembler.toModel(response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
