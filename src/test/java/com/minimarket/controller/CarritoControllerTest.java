package com.minimarket.controller;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.carrito.CarritoResponseDTO;
import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.entity.Carrito;
import com.minimarket.hateoas.CarritoModelAssembler;
import com.minimarket.mapper.CarritoMapper;
import com.minimarket.service.CarritoService;
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
class CarritoControllerTest {

    @Mock
    private CarritoService carritoService;

    @Mock
    private CarritoMapper carritoMapper;

    @Mock
    private CarritoModelAssembler carritoModelAssembler;

    @Mock
    private PagedResourcesAssembler<CarritoResponseDTO> pagedAssembler;

    @InjectMocks
    private CarritoController carritoController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Carrito THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarCarrito_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Carrito> items = List.of(new Carrito());
        Page<Carrito> entityPage = new PageImpl<>(items, pageable, items.size());
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<CarritoResponseDTO>> pagedModel = PagedModel.empty();

        when(carritoService.findAll(pageable)).thenReturn(entityPage);
        when(carritoMapper.toResponse(any(Carrito.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(carritoModelAssembler))).thenReturn(pagedModel);

        assertThat(carritoController.listarCarrito(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Carrito Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerCarritoPorId_thenRetornaOk() {
        Carrito carrito = new Carrito();
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);
        EntityModel<CarritoResponseDTO> model = EntityModel.of(dto);
        when(carritoService.findById(1L)).thenReturn(carrito);
        when(carritoMapper.toResponse(carrito)).thenReturn(dto);
        when(carritoModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<CarritoResponseDTO>> response = carritoController.obtenerCarritoPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Carrito Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerCarritoPorId_thenRetorna404() {
        when(carritoService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<CarritoResponseDTO>> response = carritoController.obtenerCarritoPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Agregar Producto Al Carrito THEN Retorna EntityModel")
    void givenDefaultContext_whenAgregarProductoAlCarrito_thenRetornaEntityModel() {
        CarritoRequestDTO request = new CarritoRequestDTO();
        IdRefDTO usuarioRef = new IdRefDTO();
        usuarioRef.setId(1L);
        IdRefDTO productoRef = new IdRefDTO();
        productoRef.setId(10L);
        request.setUsuario(usuarioRef);
        request.setProducto(productoRef);
        request.setCantidad(2);

        Carrito saved = new Carrito();
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);
        EntityModel<CarritoResponseDTO> model = EntityModel.of(dto);
        when(carritoService.agregarProducto(1L, 10L, 2)).thenReturn(saved);
        when(carritoMapper.toResponse(saved)).thenReturn(dto);
        when(carritoModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(carritoController.agregarProductoAlCarrito(request)).isSameAs(model);
        verify(carritoService).agregarProducto(1L, 10L, 2);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Carrito THEN Retorna Ok")
    void givenExiste_whenActualizarCarrito_thenRetornaOk() {
        CarritoRequestDTO request = new CarritoRequestDTO();
        Carrito saved = new Carrito();
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);
        EntityModel<CarritoResponseDTO> model = EntityModel.of(dto);
        when(carritoService.findById(1L)).thenReturn(new Carrito());
        when(carritoMapper.toEntity(request)).thenReturn(new Carrito());
        when(carritoService.save(any(Carrito.class))).thenReturn(saved);
        when(carritoMapper.toResponse(saved)).thenReturn(dto);
        when(carritoModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<CarritoResponseDTO>> response = carritoController.actualizarCarrito(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
        assertThat(request.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Carrito THEN Retorna404")
    void givenNoExiste_whenActualizarCarrito_thenRetorna404() {
        when(carritoService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<CarritoResponseDTO>> response =
                carritoController.actualizarCarrito(99L, new CarritoRequestDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Producto Del Carrito THEN Retorna204")
    void givenExiste_whenEliminarProductoDelCarrito_thenRetorna204() {
        when(carritoService.findById(1L)).thenReturn(new Carrito());

        ResponseEntity<Void> response = carritoController.eliminarProductoDelCarrito(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(carritoService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Producto Del Carrito THEN Retorna404")
    void givenNoExiste_whenEliminarProductoDelCarrito_thenRetorna404() {
        when(carritoService.findById(99L)).thenReturn(null);

        ResponseEntity<Void> response = carritoController.eliminarProductoDelCarrito(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
