package com.minimarket.controller;

import com.minimarket.dto.ordencompra.OrdenCompraResponseDTO;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.hateoas.OrdenCompraModelAssembler;
import com.minimarket.mapper.OrdenCompraMapper;
import com.minimarket.service.OrdenCompraService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenCompraControllerTest {

    @Mock
    private OrdenCompraService ordenCompraService;

    @Mock
    private OrdenCompraMapper ordenCompraMapper;

    @Mock
    private OrdenCompraModelAssembler ordenCompraModelAssembler;

    @Mock
    private PagedResourcesAssembler<OrdenCompraResponseDTO> pagedAssembler;

    @InjectMocks
    private OrdenCompraController ordenCompraController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Ordenes THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarOrdenes_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdenCompra> entityPage = new PageImpl<>(List.of(new OrdenCompra()), pageable, 1);
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<OrdenCompraResponseDTO>> pagedModel = PagedModel.empty();

        when(ordenCompraService.findAll(pageable)).thenReturn(entityPage);
        when(ordenCompraMapper.toResponse(any(OrdenCompra.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(ordenCompraModelAssembler))).thenReturn(pagedModel);

        assertThat(ordenCompraController.listarOrdenesCompra(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Orden Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerOrdenPorId_thenRetornaOk() {
        OrdenCompra entity = new OrdenCompra();
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(1L);
        EntityModel<OrdenCompraResponseDTO> model = EntityModel.of(dto);
        when(ordenCompraService.findById(1L)).thenReturn(entity);
        when(ordenCompraMapper.toResponse(entity)).thenReturn(dto);
        when(ordenCompraModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<OrdenCompraResponseDTO>> response =
                ordenCompraController.obtenerOrdenCompraPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Orden Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerOrdenPorId_thenRetorna404() {
        when(ordenCompraService.findById(99L)).thenReturn(null);

        assertThat(ordenCompraController.obtenerOrdenCompraPorId(99L).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Stock Bajo WHEN Generar Ordenes Automaticas THEN Retorna Lista EntityModel")
    void givenStockBajo_whenGenerarOrdenesAutomaticas_thenRetornaListaEntityModel() {
        OrdenCompra entity = new OrdenCompra();
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(1L);
        EntityModel<OrdenCompraResponseDTO> model = EntityModel.of(dto);
        when(ordenCompraService.generarOrdenesAutomaticas()).thenReturn(List.of(entity));
        when(ordenCompraMapper.toResponse(entity)).thenReturn(dto);
        when(ordenCompraModelAssembler.toModel(dto)).thenReturn(model);

        List<EntityModel<OrdenCompraResponseDTO>> result = ordenCompraController.generarOrdenesAutomaticas();

        assertThat(result).containsExactly(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Confirmar Recepcion THEN Retorna Ok")
    void givenExiste_whenConfirmarRecepcion_thenRetornaOk() {
        OrdenCompra entity = new OrdenCompra();
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(1L);
        EntityModel<OrdenCompraResponseDTO> model = EntityModel.of(dto);
        when(ordenCompraService.confirmarRecepcion(1L)).thenReturn(entity);
        when(ordenCompraMapper.toResponse(entity)).thenReturn(dto);
        when(ordenCompraModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<OrdenCompraResponseDTO>> response =
                ordenCompraController.confirmarRecepcion(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Confirmar Recepcion THEN Retorna404")
    void givenNoExiste_whenConfirmarRecepcion_thenRetorna404() {
        when(ordenCompraService.confirmarRecepcion(99L)).thenThrow(new IllegalArgumentException("no existe"));

        assertThat(ordenCompraController.confirmarRecepcion(99L).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Ya Recibida WHEN Confirmar Recepcion THEN Retorna400")
    void givenYaRecibida_whenConfirmarRecepcion_thenRetorna400() {
        when(ordenCompraService.confirmarRecepcion(1L)).thenThrow(new IllegalStateException("ya recibida"));

        assertThat(ordenCompraController.confirmarRecepcion(1L).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
