package com.minimarket.controller;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.carrito.CarritoResponseDTO;
import com.minimarket.hateoas.CarritoModelAssembler;
import com.minimarket.mapper.CarritoMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.CarritoService;
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

@Tag(name = "Carrito", description = "Carrito de compras por usuario autenticado")
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoMapper carritoMapper;

    @Autowired
    private CarritoModelAssembler carritoModelAssembler;

    @Operation(summary = "Listar items del carrito",
            description = "Lista paginada HAL con _embedded, page y links de paginación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de items del carrito"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public PagedModel<EntityModel<CarritoResponseDTO>> listarCarrito(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<CarritoResponseDTO> pagedAssembler) {
        Page<CarritoResponseDTO> page = carritoService.findAll(pageable).map(carritoMapper::toResponse);
        return pagedAssembler.toModel(page, carritoModelAssembler);
    }

    @Operation(summary = "Obtener item del carrito por ID",
            description = "Respuesta HAL con _links (self, carrito, usuario, producto).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<EntityModel<CarritoResponseDTO>> obtenerCarritoPorId(
            @Parameter(description = "ID del item en carrito", example = "1") @PathVariable Long id) {
        var carrito = carritoService.findById(id);
        return (carrito != null)
                ? ResponseEntity.ok(carritoModelAssembler.toModel(carritoMapper.toResponse(carrito)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Agregar producto al carrito",
            description = "Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto agregado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public EntityModel<CarritoResponseDTO> agregarProductoAlCarrito(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Producto y cantidad a agregar",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CarritoRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Agregar al carrito",
                                    value = """
                                            {
                                              "usuario": { "id": 1 },
                                              "producto": { "id": 1 },
                                              "cantidad": 2
                                            }
                                            """)))
            @Valid @RequestBody CarritoRequestDTO carritoDto) {
        CarritoResponseDTO response = carritoMapper.toResponse(carritoService.agregarProducto(
                carritoDto.getUsuario().getId(),
                carritoDto.getProducto().getId(),
                carritoDto.getCantidad()));
        return carritoModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar item del carrito",
            description = "Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<EntityModel<CarritoResponseDTO>> actualizarCarrito(
            @Parameter(description = "ID del item en carrito", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del item",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CarritoRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Actualizar cantidad",
                                    value = """
                                            {
                                              "usuario": { "id": 1 },
                                              "producto": { "id": 1 },
                                              "cantidad": 3
                                            }
                                            """)))
            @Valid @RequestBody CarritoRequestDTO carritoDto) {
        var existente = carritoService.findById(id);
        if (existente != null) {
            carritoDto.setId(id);
            CarritoResponseDTO response = carritoMapper.toResponse(
                    carritoService.save(carritoMapper.toEntity(carritoDto)));
            return ResponseEntity.ok(carritoModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar producto del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(description = "ID del item en carrito", example = "1") @PathVariable Long id) {
        var carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
