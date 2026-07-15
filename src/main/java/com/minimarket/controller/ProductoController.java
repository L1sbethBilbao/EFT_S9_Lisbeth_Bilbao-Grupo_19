package com.minimarket.controller;

import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.hateoas.ProductoModelAssembler;
import com.minimarket.mapper.ProductoMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.ProductoService;
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

@Tag(name = "Productos", description = "Catálogo y gestión de productos del minimarket")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoMapper productoMapper;

    @Autowired
    private ProductoModelAssembler productoModelAssembler;

    @Operation(summary = "Listar productos",
            description = "Consulta pública paginada del catálogo. Respuesta HAL con _embedded, page y links de paginación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos con HATEOAS",
                    content = @Content(schema = @Schema(implementation = ProductoResponseDTO.class)))
    })
    @SecurityRequirements
    @GetMapping
    public PagedModel<EntityModel<ProductoResponseDTO>> listarProductos(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<ProductoResponseDTO> pagedAssembler) {
        Page<ProductoResponseDTO> page = productoService.findAll(pageable).map(productoMapper::toResponse);
        return pagedAssembler.toModel(page, productoModelAssembler);
    }

    @Operation(summary = "Obtener producto por ID",
            description = "Respuesta HAL con _links (self, productos, categoria).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @SecurityRequirements
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProductoResponseDTO>> obtenerProductoPorId(
            @Parameter(description = "ID del producto", example = "1") @PathVariable Long id) {
        var producto = productoService.findById(id);
        return (producto != null)
                ? ResponseEntity.ok(productoModelAssembler.toModel(productoMapper.toResponse(producto)))
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Crear producto", description = "Solo rol GERENTE. Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public EntityModel<ProductoResponseDTO> guardarProducto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Producto a crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductoRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Producto demo",
                                    value = """
                                            {
                                              "nombre": "Galletas surtidas 200g",
                                              "precio": 990,
                                              "stock": 25,
                                              "descripcion": "Snack dulce",
                                              "categoria": { "id": 1 }
                                            }
                                            """)))
            @Valid @RequestBody ProductoRequestDTO productoDto) {
        ProductoResponseDTO response = productoMapper.toResponse(
                productoService.save(productoMapper.toEntity(productoDto)));
        return productoModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar producto", description = "Solo rol GERENTE. Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<EntityModel<ProductoResponseDTO>> actualizarProducto(
            @Parameter(description = "ID del producto", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del producto",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductoRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Actualizar stock",
                                    value = """
                                            {
                                              "nombre": "Arroz grano largo 1kg",
                                              "precio": 1890,
                                              "stock": 15,
                                              "descripcion": "Stock actualizado",
                                              "categoria": { "id": 1 }
                                            }
                                            """)))
            @Valid @RequestBody ProductoRequestDTO productoDto) {
        var productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            productoDto.setId(id);
            ProductoResponseDTO response = productoMapper.toResponse(
                    productoService.save(productoMapper.toEntity(productoDto)));
            return ResponseEntity.ok(productoModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar producto", description = "Solo rol GERENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto", example = "1") @PathVariable Long id) {
        var producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
