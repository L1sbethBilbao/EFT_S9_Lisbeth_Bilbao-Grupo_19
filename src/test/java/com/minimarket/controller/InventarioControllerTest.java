package com.minimarket.controller;

import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.dto.inventario.InventarioResponseDTO;
import com.minimarket.entity.Inventario;
import com.minimarket.hateoas.InventarioModelAssembler;
import com.minimarket.mapper.InventarioMapper;
import com.minimarket.service.InventarioService;
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
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @Mock
    private InventarioMapper inventarioMapper;

    @Mock
    private InventarioModelAssembler inventarioModelAssembler;

    @Mock
    private PagedResourcesAssembler<InventarioResponseDTO> pagedAssembler;

    @InjectMocks
    private InventarioController inventarioController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Movimientos De Inventario THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarMovimientosDeInventario_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Inventario> movimientos = List.of(new Inventario());
        Page<Inventario> entityPage = new PageImpl<>(movimientos, pageable, movimientos.size());
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<InventarioResponseDTO>> pagedModel = PagedModel.empty();

        when(inventarioService.findAll(pageable)).thenReturn(entityPage);
        when(inventarioMapper.toResponse(any(Inventario.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(inventarioModelAssembler))).thenReturn(pagedModel);

        assertThat(inventarioController.listarMovimientosDeInventario(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Movimiento Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerMovimientoPorId_thenRetornaOk() {
        Inventario inventario = new Inventario();
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);
        EntityModel<InventarioResponseDTO> model = EntityModel.of(dto);
        when(inventarioService.findById(1L)).thenReturn(inventario);
        when(inventarioMapper.toResponse(inventario)).thenReturn(dto);
        when(inventarioModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<InventarioResponseDTO>> response = inventarioController.obtenerMovimientoPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Movimiento Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerMovimientoPorId_thenRetorna404() {
        when(inventarioService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<InventarioResponseDTO>> response = inventarioController.obtenerMovimientoPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Registrar Movimiento THEN Retorna EntityModel")
    void givenDefaultContext_whenRegistrarMovimiento_thenRetornaEntityModel() {
        InventarioRequestDTO request = new InventarioRequestDTO();
        Inventario entity = new Inventario();
        Inventario saved = new Inventario();
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);
        EntityModel<InventarioResponseDTO> model = EntityModel.of(dto);
        when(inventarioMapper.toEntity(request)).thenReturn(entity);
        when(inventarioService.save(entity)).thenReturn(saved);
        when(inventarioMapper.toResponse(saved)).thenReturn(dto);
        when(inventarioModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(inventarioController.registrarMovimiento(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Movimiento THEN Retorna Ok")
    void givenExiste_whenActualizarMovimiento_thenRetornaOk() {
        InventarioRequestDTO request = new InventarioRequestDTO();
        Inventario saved = new Inventario();
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);
        EntityModel<InventarioResponseDTO> model = EntityModel.of(dto);
        when(inventarioService.findById(1L)).thenReturn(new Inventario());
        when(inventarioMapper.toEntity(request)).thenReturn(new Inventario());
        when(inventarioService.save(any(Inventario.class))).thenReturn(saved);
        when(inventarioMapper.toResponse(saved)).thenReturn(dto);
        when(inventarioModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<InventarioResponseDTO>> response = inventarioController.actualizarMovimiento(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
        assertThat(request.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Movimiento THEN Retorna404")
    void givenNoExiste_whenActualizarMovimiento_thenRetorna404() {
        when(inventarioService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<InventarioResponseDTO>> response =
                inventarioController.actualizarMovimiento(99L, new InventarioRequestDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Movimiento THEN Retorna204")
    void givenExiste_whenEliminarMovimiento_thenRetorna204() {
        when(inventarioService.findById(1L)).thenReturn(new Inventario());

        ResponseEntity<Void> response = inventarioController.eliminarMovimiento(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(inventarioService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Movimiento THEN Retorna404")
    void givenNoExiste_whenEliminarMovimiento_thenRetorna404() {
        when(inventarioService.findById(99L)).thenReturn(null);

        ResponseEntity<Void> response = inventarioController.eliminarMovimiento(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
