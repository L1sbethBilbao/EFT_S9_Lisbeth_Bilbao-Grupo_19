package com.minimarket.controller;

import com.minimarket.dto.sucursal.SucursalRequestDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;
import com.minimarket.entity.Sucursal;
import com.minimarket.hateoas.SucursalModelAssembler;
import com.minimarket.mapper.SucursalMapper;
import com.minimarket.service.SucursalService;
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
class SucursalControllerTest {

    @Mock
    private SucursalService sucursalService;

    @Mock
    private SucursalMapper sucursalMapper;

    @Mock
    private SucursalModelAssembler sucursalModelAssembler;

    @Mock
    private PagedResourcesAssembler<SucursalResponseDTO> pagedAssembler;

    @InjectMocks
    private SucursalController sucursalController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Sucursales THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarSucursales_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Sucursal> entityPage = new PageImpl<>(List.of(new Sucursal()), pageable, 1);
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<SucursalResponseDTO>> pagedModel = PagedModel.empty();

        when(sucursalService.findAll(pageable)).thenReturn(entityPage);
        when(sucursalMapper.toResponse(any(Sucursal.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(sucursalModelAssembler))).thenReturn(pagedModel);

        assertThat(sucursalController.listarSucursales(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Sucursal Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerSucursalPorId_thenRetornaOk() {
        Sucursal entity = new Sucursal();
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(1L);
        EntityModel<SucursalResponseDTO> model = EntityModel.of(dto);
        when(sucursalService.findById(1L)).thenReturn(entity);
        when(sucursalMapper.toResponse(entity)).thenReturn(dto);
        when(sucursalModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<SucursalResponseDTO>> response = sucursalController.obtenerSucursalPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Sucursal Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerSucursalPorId_thenRetorna404() {
        when(sucursalService.findById(99L)).thenReturn(null);

        assertThat(sucursalController.obtenerSucursalPorId(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Sucursal THEN Retorna EntityModel")
    void givenDefaultContext_whenGuardarSucursal_thenRetornaEntityModel() {
        SucursalRequestDTO request = new SucursalRequestDTO();
        Sucursal entity = new Sucursal();
        Sucursal saved = new Sucursal();
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(1L);
        EntityModel<SucursalResponseDTO> model = EntityModel.of(dto);
        when(sucursalMapper.toEntity(request)).thenReturn(entity);
        when(sucursalService.save(entity)).thenReturn(saved);
        when(sucursalMapper.toResponse(saved)).thenReturn(dto);
        when(sucursalModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(sucursalController.guardarSucursal(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Sucursal THEN Retorna Ok")
    void givenExiste_whenActualizarSucursal_thenRetornaOk() {
        SucursalRequestDTO request = new SucursalRequestDTO();
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(1L);
        EntityModel<SucursalResponseDTO> model = EntityModel.of(dto);
        when(sucursalService.findById(1L)).thenReturn(new Sucursal());
        when(sucursalMapper.toEntity(request)).thenReturn(new Sucursal());
        when(sucursalService.save(any(Sucursal.class))).thenReturn(new Sucursal());
        when(sucursalMapper.toResponse(any(Sucursal.class))).thenReturn(dto);
        when(sucursalModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<SucursalResponseDTO>> response = sucursalController.actualizarSucursal(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(request.getId()).isEqualTo(1L);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Sucursal THEN Retorna404")
    void givenNoExiste_whenActualizarSucursal_thenRetorna404() {
        when(sucursalService.findById(99L)).thenReturn(null);

        assertThat(sucursalController.actualizarSucursal(99L, new SucursalRequestDTO()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Sucursal THEN Retorna204")
    void givenExiste_whenEliminarSucursal_thenRetorna204() {
        when(sucursalService.findById(1L)).thenReturn(new Sucursal());

        ResponseEntity<Void> response = sucursalController.eliminarSucursal(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(sucursalService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Sucursal THEN Retorna404")
    void givenNoExiste_whenEliminarSucursal_thenRetorna404() {
        when(sucursalService.findById(99L)).thenReturn(null);

        assertThat(sucursalController.eliminarSucursal(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
