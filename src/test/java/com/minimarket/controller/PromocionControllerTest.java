package com.minimarket.controller;

import com.minimarket.dto.promocion.PromocionRequestDTO;
import com.minimarket.dto.promocion.PromocionResponseDTO;
import com.minimarket.entity.Promocion;
import com.minimarket.hateoas.PromocionModelAssembler;
import com.minimarket.mapper.PromocionMapper;
import com.minimarket.service.PromocionService;
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
class PromocionControllerTest {

    @Mock
    private PromocionService promocionService;

    @Mock
    private PromocionMapper promocionMapper;

    @Mock
    private PromocionModelAssembler promocionModelAssembler;

    @Mock
    private PagedResourcesAssembler<PromocionResponseDTO> pagedAssembler;

    @InjectMocks
    private PromocionController promocionController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Promociones THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarPromociones_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Promocion> entityPage = new PageImpl<>(List.of(new Promocion()), pageable, 1);
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<PromocionResponseDTO>> pagedModel = PagedModel.empty();

        when(promocionService.findAll(pageable)).thenReturn(entityPage);
        when(promocionMapper.toResponse(any(Promocion.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(promocionModelAssembler))).thenReturn(pagedModel);

        assertThat(promocionController.listarPromociones(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Activas WHEN Listar Promociones Activas THEN Retorna Lista")
    void givenActivas_whenListarPromocionesActivas_thenRetornaLista() {
        List<Promocion> entities = List.of(new Promocion());
        List<PromocionResponseDTO> dtos = List.of(new PromocionResponseDTO());
        when(promocionService.findActivas()).thenReturn(entities);
        when(promocionMapper.toResponseList(entities)).thenReturn(dtos);

        assertThat(promocionController.listarPromocionesActivas()).isSameAs(dtos);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Promocion Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerPromocionPorId_thenRetornaOk() {
        Promocion entity = new Promocion();
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(1L);
        EntityModel<PromocionResponseDTO> model = EntityModel.of(dto);
        when(promocionService.findById(1L)).thenReturn(entity);
        when(promocionMapper.toResponse(entity)).thenReturn(dto);
        when(promocionModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<PromocionResponseDTO>> response = promocionController.obtenerPromocionPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Promocion Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerPromocionPorId_thenRetorna404() {
        when(promocionService.findById(99L)).thenReturn(null);

        assertThat(promocionController.obtenerPromocionPorId(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Promocion THEN Retorna EntityModel")
    void givenDefaultContext_whenGuardarPromocion_thenRetornaEntityModel() {
        PromocionRequestDTO request = new PromocionRequestDTO();
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(1L);
        EntityModel<PromocionResponseDTO> model = EntityModel.of(dto);
        when(promocionMapper.toEntity(request)).thenReturn(new Promocion());
        when(promocionService.save(any(Promocion.class))).thenReturn(new Promocion());
        when(promocionMapper.toResponse(any(Promocion.class))).thenReturn(dto);
        when(promocionModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(promocionController.guardarPromocion(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Promocion THEN Retorna Ok")
    void givenExiste_whenActualizarPromocion_thenRetornaOk() {
        PromocionRequestDTO request = new PromocionRequestDTO();
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(1L);
        EntityModel<PromocionResponseDTO> model = EntityModel.of(dto);
        when(promocionService.findById(1L)).thenReturn(new Promocion());
        when(promocionMapper.toEntity(request)).thenReturn(new Promocion());
        when(promocionService.save(any(Promocion.class))).thenReturn(new Promocion());
        when(promocionMapper.toResponse(any(Promocion.class))).thenReturn(dto);
        when(promocionModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<PromocionResponseDTO>> response =
                promocionController.actualizarPromocion(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(request.getId()).isEqualTo(1L);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Promocion THEN Retorna404")
    void givenNoExiste_whenActualizarPromocion_thenRetorna404() {
        when(promocionService.findById(99L)).thenReturn(null);

        assertThat(promocionController.actualizarPromocion(99L, new PromocionRequestDTO()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Promocion THEN Retorna204")
    void givenExiste_whenEliminarPromocion_thenRetorna204() {
        when(promocionService.findById(1L)).thenReturn(new Promocion());

        ResponseEntity<Void> response = promocionController.eliminarPromocion(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(promocionService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Promocion THEN Retorna404")
    void givenNoExiste_whenEliminarPromocion_thenRetorna404() {
        when(promocionService.findById(99L)).thenReturn(null);

        assertThat(promocionController.eliminarPromocion(99L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
