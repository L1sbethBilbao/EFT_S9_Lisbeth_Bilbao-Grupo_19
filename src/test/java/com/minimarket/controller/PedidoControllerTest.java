package com.minimarket.controller;

import com.minimarket.dto.pedido.PedidoEstadoUpdateDTO;
import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.dto.pedido.PedidoResponseDTO;
import com.minimarket.entity.Pedido;
import com.minimarket.hateoas.PedidoModelAssembler;
import com.minimarket.mapper.PedidoMapper;
import com.minimarket.service.PedidoService;
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
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private PedidoModelAssembler pedidoModelAssembler;

    @Mock
    private PagedResourcesAssembler<PedidoResponseDTO> pagedAssembler;

    @InjectMocks
    private PedidoController pedidoController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Pedidos THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarPedidos_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> entityPage = new PageImpl<>(List.of(new Pedido()), pageable, 1);
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<PedidoResponseDTO>> pagedModel = PagedModel.empty();

        when(pedidoService.findAll(pageable)).thenReturn(entityPage);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(pedidoModelAssembler))).thenReturn(pagedModel);

        assertThat(pedidoController.listarPedidos(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Pedido Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerPedidoPorId_thenRetornaOk() {
        Pedido entity = new Pedido();
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        EntityModel<PedidoResponseDTO> model = EntityModel.of(dto);
        when(pedidoService.findById(1L)).thenReturn(entity);
        when(pedidoMapper.toResponse(entity)).thenReturn(dto);
        when(pedidoModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<PedidoResponseDTO>> response = pedidoController.obtenerPedidoPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Pedido Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerPedidoPorId_thenRetorna404() {
        when(pedidoService.findById(99L)).thenReturn(null);

        assertThat(pedidoController.obtenerPedidoPorId(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Registrar Pedido THEN Retorna EntityModel")
    void givenDefaultContext_whenRegistrarPedido_thenRetornaEntityModel() {
        PedidoRegistroDTO request = new PedidoRegistroDTO();
        Pedido entity = new Pedido();
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        EntityModel<PedidoResponseDTO> model = EntityModel.of(dto);
        when(pedidoService.registrarPedido(request)).thenReturn(entity);
        when(pedidoMapper.toResponse(entity)).thenReturn(dto);
        when(pedidoModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(pedidoController.registrarPedido(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Estado THEN Retorna Ok")
    void givenExiste_whenActualizarEstado_thenRetornaOk() {
        PedidoEstadoUpdateDTO request = new PedidoEstadoUpdateDTO();
        request.setEstado("CONFIRMADO");
        Pedido entity = new Pedido();
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        EntityModel<PedidoResponseDTO> model = EntityModel.of(dto);
        when(pedidoService.actualizarEstado(1L, "CONFIRMADO")).thenReturn(entity);
        when(pedidoMapper.toResponse(entity)).thenReturn(dto);
        when(pedidoModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<PedidoResponseDTO>> response = pedidoController.actualizarEstado(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Estado THEN Retorna404")
    void givenNoExiste_whenActualizarEstado_thenRetorna404() {
        PedidoEstadoUpdateDTO request = new PedidoEstadoUpdateDTO();
        request.setEstado("CONFIRMADO");
        when(pedidoService.actualizarEstado(99L, "CONFIRMADO")).thenThrow(new IllegalArgumentException("no existe"));

        assertThat(pedidoController.actualizarEstado(99L, request).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
