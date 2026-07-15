package com.minimarket.controller;

import com.minimarket.dto.detalleventa.DetalleVentaRequestDTO;
import com.minimarket.dto.detalleventa.DetalleVentaResponseDTO;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.mapper.DetalleVentaMapper;
import com.minimarket.service.DetalleVentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetalleVentaControllerTest {

    @Mock
    private DetalleVentaService detalleVentaService;

    @Mock
    private DetalleVentaMapper detalleVentaMapper;

    @InjectMocks
    private DetalleVentaController detalleVentaController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Detalle Ventas THEN Retorna Lista Mapeada")
    void givenDefaultContext_whenListarDetalleVentas_thenRetornaListaMapeada() {
        List<DetalleVenta> detalles = List.of(new DetalleVenta());
        List<DetalleVentaResponseDTO> dtos = List.of(new DetalleVentaResponseDTO());
        when(detalleVentaService.findAll()).thenReturn(detalles);
        when(detalleVentaMapper.toResponseList(detalles)).thenReturn(dtos);

        assertThat(detalleVentaController.listarDetalleVentas()).isSameAs(dtos);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Detalle Venta Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerDetalleVentaPorId_thenRetornaOk() {
        DetalleVenta detalle = new DetalleVenta();
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        when(detalleVentaService.findById(1L)).thenReturn(detalle);
        when(detalleVentaMapper.toResponse(detalle)).thenReturn(dto);

        ResponseEntity<DetalleVentaResponseDTO> response = detalleVentaController.obtenerDetalleVentaPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Detalle Venta Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerDetalleVentaPorId_thenRetorna404() {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        ResponseEntity<DetalleVentaResponseDTO> response = detalleVentaController.obtenerDetalleVentaPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Detalle Venta THEN Retorna Dto")
    void givenDefaultContext_whenGuardarDetalleVenta_thenRetornaDto() {
        DetalleVentaRequestDTO request = new DetalleVentaRequestDTO();
        DetalleVenta entity = new DetalleVenta();
        DetalleVenta saved = new DetalleVenta();
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        when(detalleVentaMapper.toEntity(request)).thenReturn(entity);
        when(detalleVentaService.save(entity)).thenReturn(saved);
        when(detalleVentaMapper.toResponse(saved)).thenReturn(dto);

        assertThat(detalleVentaController.guardarDetalleVenta(request)).isSameAs(dto);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Detalle Venta THEN Retorna Ok")
    void givenExiste_whenActualizarDetalleVenta_thenRetornaOk() {
        DetalleVentaRequestDTO request = new DetalleVentaRequestDTO();
        DetalleVenta saved = new DetalleVenta();
        DetalleVentaResponseDTO dto = new DetalleVentaResponseDTO();
        when(detalleVentaService.findById(1L)).thenReturn(new DetalleVenta());
        when(detalleVentaMapper.toEntity(request)).thenReturn(new DetalleVenta());
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(saved);
        when(detalleVentaMapper.toResponse(saved)).thenReturn(dto);

        ResponseEntity<DetalleVentaResponseDTO> response =
                detalleVentaController.actualizarDetalleVenta(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
        assertThat(request.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Detalle Venta THEN Retorna404")
    void givenNoExiste_whenActualizarDetalleVenta_thenRetorna404() {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        ResponseEntity<DetalleVentaResponseDTO> response =
                detalleVentaController.actualizarDetalleVenta(99L, new DetalleVentaRequestDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Detalle Venta THEN Retorna204")
    void givenExiste_whenEliminarDetalleVenta_thenRetorna204() {
        when(detalleVentaService.findById(1L)).thenReturn(new DetalleVenta());

        ResponseEntity<Void> response = detalleVentaController.eliminarDetalleVenta(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(detalleVentaService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Detalle Venta THEN Retorna404")
    void givenNoExiste_whenEliminarDetalleVenta_thenRetorna404() {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        ResponseEntity<Void> response = detalleVentaController.eliminarDetalleVenta(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
