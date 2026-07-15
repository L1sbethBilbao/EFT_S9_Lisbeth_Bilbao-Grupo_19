package com.minimarket.controller;

import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.entity.Producto;
import com.minimarket.hateoas.ProductoModelAssembler;
import com.minimarket.mapper.ProductoMapper;
import com.minimarket.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private ProductoModelAssembler productoModelAssembler;

    @Mock
    private PagedResourcesAssembler<ProductoResponseDTO> pagedAssembler;

    @InjectMocks
    private ProductoController productoController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Productos THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarProductos_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Producto> productos = List.of(new Producto());
        Page<Producto> entityPage = new PageImpl<>(productos, pageable, productos.size());
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<ProductoResponseDTO>> pagedModel = PagedModel.empty();

        when(productoService.findAll(pageable)).thenReturn(entityPage);
        when(productoMapper.toResponse(any(Producto.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(productoModelAssembler))).thenReturn(pagedModel);

        assertThat(productoController.listarProductos(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Producto Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerProductoPorId_thenRetornaOk() {
        Producto producto = new Producto();
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);
        EntityModel<ProductoResponseDTO> model = EntityModel.of(dto);
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoMapper.toResponse(producto)).thenReturn(dto);
        when(productoModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<ProductoResponseDTO>> response = productoController.obtenerProductoPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Producto Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerProductoPorId_thenRetorna404() {
        when(productoService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<ProductoResponseDTO>> response = productoController.obtenerProductoPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Producto THEN Retorna EntityModel")
    void givenDefaultContext_whenGuardarProducto_thenRetornaEntityModel() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        Producto entity = new Producto();
        Producto saved = new Producto();
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);
        EntityModel<ProductoResponseDTO> model = EntityModel.of(dto);
        when(productoMapper.toEntity(request)).thenReturn(entity);
        when(productoService.save(entity)).thenReturn(saved);
        when(productoMapper.toResponse(saved)).thenReturn(dto);
        when(productoModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(productoController.guardarProducto(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Producto THEN Retorna Ok")
    void givenExiste_whenActualizarProducto_thenRetornaOk() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        Producto existente = new Producto();
        Producto saved = new Producto();
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);
        EntityModel<ProductoResponseDTO> model = EntityModel.of(dto);
        when(productoService.findById(1L)).thenReturn(existente);
        when(productoMapper.toEntity(request)).thenReturn(new Producto());
        when(productoService.save(any(Producto.class))).thenReturn(saved);
        when(productoMapper.toResponse(saved)).thenReturn(dto);
        when(productoModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<ProductoResponseDTO>> response = productoController.actualizarProducto(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
        assertThat(request.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Producto THEN Retorna404")
    void givenNoExiste_whenActualizarProducto_thenRetorna404() {
        when(productoService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<ProductoResponseDTO>> response =
                productoController.actualizarProducto(99L, new ProductoRequestDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Producto THEN Retorna204")
    void givenExiste_whenEliminarProducto_thenRetorna204() {
        when(productoService.findById(1L)).thenReturn(new Producto());

        ResponseEntity<Void> response = productoController.eliminarProducto(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(productoService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Producto THEN Retorna404")
    void givenNoExiste_whenEliminarProducto_thenRetorna404() {
        when(productoService.findById(99L)).thenReturn(null);

        ResponseEntity<Void> response = productoController.eliminarProducto(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
