package com.minimarket.controller;

import com.minimarket.dto.stocksucursal.DisponibilidadResponseDTO;
import com.minimarket.dto.stocksucursal.StockSucursalRequestDTO;
import com.minimarket.dto.stocksucursal.StockSucursalResponseDTO;
import com.minimarket.entity.StockSucursal;
import com.minimarket.hateoas.StockSucursalModelAssembler;
import com.minimarket.mapper.StockSucursalMapper;
import com.minimarket.service.StockSucursalService;
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
class StockSucursalControllerTest {

    @Mock
    private StockSucursalService stockSucursalService;

    @Mock
    private StockSucursalMapper stockSucursalMapper;

    @Mock
    private StockSucursalModelAssembler stockSucursalModelAssembler;

    @Mock
    private PagedResourcesAssembler<StockSucursalResponseDTO> pagedAssembler;

    @InjectMocks
    private StockSucursalController stockSucursalController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Stock THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarStock_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockSucursal> entityPage = new PageImpl<>(List.of(new StockSucursal()), pageable, 1);
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<StockSucursalResponseDTO>> pagedModel = PagedModel.empty();

        when(stockSucursalService.findAll(pageable)).thenReturn(entityPage);
        when(stockSucursalMapper.toResponse(any(StockSucursal.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(stockSucursalModelAssembler))).thenReturn(pagedModel);

        assertThat(stockSucursalController.listarStockSucursal(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Sucursal WHEN Consultar Disponibilidad THEN Retorna Lista")
    void givenSucursal_whenConsultarDisponibilidad_thenRetornaLista() {
        List<DisponibilidadResponseDTO> lista = List.of(new DisponibilidadResponseDTO());
        when(stockSucursalService.consultarDisponibilidad(1L)).thenReturn(lista);

        assertThat(stockSucursalController.consultarDisponibilidad(1L)).isSameAs(lista);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Stock Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerStockPorId_thenRetornaOk() {
        StockSucursal entity = new StockSucursal();
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(1L);
        EntityModel<StockSucursalResponseDTO> model = EntityModel.of(dto);
        when(stockSucursalService.findById(1L)).thenReturn(entity);
        when(stockSucursalMapper.toResponse(entity)).thenReturn(dto);
        when(stockSucursalModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<StockSucursalResponseDTO>> response = stockSucursalController.obtenerStockPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Stock Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerStockPorId_thenRetorna404() {
        when(stockSucursalService.findById(99L)).thenReturn(null);

        assertThat(stockSucursalController.obtenerStockPorId(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Stock THEN Retorna EntityModel")
    void givenDefaultContext_whenGuardarStock_thenRetornaEntityModel() {
        StockSucursalRequestDTO request = new StockSucursalRequestDTO();
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(1L);
        EntityModel<StockSucursalResponseDTO> model = EntityModel.of(dto);
        when(stockSucursalMapper.toEntity(request)).thenReturn(new StockSucursal());
        when(stockSucursalService.save(any(StockSucursal.class))).thenReturn(new StockSucursal());
        when(stockSucursalMapper.toResponse(any(StockSucursal.class))).thenReturn(dto);
        when(stockSucursalModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(stockSucursalController.guardarStock(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Stock THEN Retorna Ok")
    void givenExiste_whenActualizarStock_thenRetornaOk() {
        StockSucursalRequestDTO request = new StockSucursalRequestDTO();
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(1L);
        EntityModel<StockSucursalResponseDTO> model = EntityModel.of(dto);
        when(stockSucursalService.findById(1L)).thenReturn(new StockSucursal());
        when(stockSucursalMapper.toEntity(request)).thenReturn(new StockSucursal());
        when(stockSucursalService.save(any(StockSucursal.class))).thenReturn(new StockSucursal());
        when(stockSucursalMapper.toResponse(any(StockSucursal.class))).thenReturn(dto);
        when(stockSucursalModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<StockSucursalResponseDTO>> response =
                stockSucursalController.actualizarStock(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(request.getId()).isEqualTo(1L);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Stock THEN Retorna404")
    void givenNoExiste_whenActualizarStock_thenRetorna404() {
        when(stockSucursalService.findById(99L)).thenReturn(null);

        assertThat(stockSucursalController.actualizarStock(99L, new StockSucursalRequestDTO()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Stock THEN Retorna204")
    void givenExiste_whenEliminarStock_thenRetorna204() {
        when(stockSucursalService.findById(1L)).thenReturn(new StockSucursal());

        ResponseEntity<Void> response = stockSucursalController.eliminarStock(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(stockSucursalService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Stock THEN Retorna404")
    void givenNoExiste_whenEliminarStock_thenRetorna404() {
        when(stockSucursalService.findById(99L)).thenReturn(null);

        assertThat(stockSucursalController.eliminarStock(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
