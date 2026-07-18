package com.minimarket.controller;

import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.dto.venta.VentaRequestDTO;
import com.minimarket.dto.venta.VentaResponseDTO;
import com.minimarket.entity.Venta;
import com.minimarket.hateoas.VentaModelAssembler;
import com.minimarket.mapper.VentaMapper;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
class VentaControllerTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private VentaMapper ventaMapper;

    @Mock
    private VentaModelAssembler ventaModelAssembler;

    @Mock
    private PagedResourcesAssembler<VentaResponseDTO> pagedAssembler;

    @InjectMocks
    private VentaController ventaController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Ventas THEN Retorna Pagina Hateoas")
    void givenDefaultContext_whenListarVentas_thenRetornaPaginaHateoas() {
        Venta venta = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        Page<Venta> page = new PageImpl<>(List.of(venta));
        PagedModel<EntityModel<VentaResponseDTO>> pagedModel = PagedModel.empty();

        when(ventaService.findAll(any(Pageable.class))).thenReturn(page);
        when(ventaMapper.toResponse(venta)).thenReturn(dto);
        when(pagedAssembler.toModel(any(), eq(ventaModelAssembler))).thenReturn(pagedModel);

        assertThat(ventaController.listarVentas(Pageable.unpaged(), pagedAssembler))
                .isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Venta Por Id THEN Retorna Model")
    void givenExiste_whenObtenerVentaPorId_thenRetornaModel() {
        Venta venta = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        EntityModel<VentaResponseDTO> model = EntityModel.of(dto);
        when(ventaService.findById(1L)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(dto);
        when(ventaModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<VentaResponseDTO>> response =
                ventaController.obtenerVentaPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Venta Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerVentaPorId_thenRetorna404() {
        when(ventaService.findById(99L)).thenReturn(null);

        ResponseEntity<EntityModel<VentaResponseDTO>> response =
                ventaController.obtenerVentaPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Registrar Venta THEN Retorna Model")
    void givenDefaultContext_whenRegistrarVenta_thenRetornaModel() {
        VentaRegistroDTO request = new VentaRegistroDTO();
        Venta venta = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        EntityModel<VentaResponseDTO> model = EntityModel.of(dto);
        when(ventaService.registrarVenta(request)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(dto);
        when(ventaModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(ventaController.registrarVenta(request)).isSameAs(model);
        verify(ventaService).registrarVenta(request);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Venta THEN Retorna Model")
    void givenDefaultContext_whenGuardarVenta_thenRetornaModel() {
        VentaRequestDTO request = new VentaRequestDTO();
        Venta entity = new Venta();
        Venta saved = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        EntityModel<VentaResponseDTO> model = EntityModel.of(dto);
        when(ventaMapper.toEntity(request)).thenReturn(entity);
        when(ventaService.save(entity)).thenReturn(saved);
        when(ventaMapper.toResponse(saved)).thenReturn(dto);
        when(ventaModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(ventaController.guardarVenta(request)).isSameAs(model);
    }
}
