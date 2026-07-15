package com.minimarket.controller;

import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.dto.venta.VentaRequestDTO;
import com.minimarket.dto.venta.VentaResponseDTO;
import com.minimarket.entity.Venta;
import com.minimarket.mapper.VentaMapper;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private VentaMapper ventaMapper;

    @InjectMocks
    private VentaController ventaController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Ventas THEN Retorna Lista Mapeada")
    void givenDefaultContext_whenListarVentas_thenRetornaListaMapeada() {
        List<Venta> ventas = List.of(new Venta());
        List<VentaResponseDTO> dtos = List.of(new VentaResponseDTO());
        when(ventaService.findAll()).thenReturn(ventas);
        when(ventaMapper.toResponseList(ventas)).thenReturn(dtos);

        assertThat(ventaController.listarVentas()).isSameAs(dtos);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Venta Por Id THEN Retorna Dto")
    void givenExiste_whenObtenerVentaPorId_thenRetornaDto() {
        Venta venta = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        when(ventaService.findById(1L)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(dto);

        assertThat(ventaController.obtenerVentaPorId(1L)).isSameAs(dto);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Venta Por Id THEN Retorna Null")
    void givenNoExiste_whenObtenerVentaPorId_thenRetornaNull() {
        when(ventaService.findById(99L)).thenReturn(null);

        assertThat(ventaController.obtenerVentaPorId(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Registrar Venta THEN Retorna Dto")
    void givenDefaultContext_whenRegistrarVenta_thenRetornaDto() {
        VentaRegistroDTO request = new VentaRegistroDTO();
        Venta venta = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        when(ventaService.registrarVenta(request)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(dto);

        assertThat(ventaController.registrarVenta(request)).isSameAs(dto);
        verify(ventaService).registrarVenta(request);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Venta THEN Retorna Dto")
    void givenDefaultContext_whenGuardarVenta_thenRetornaDto() {
        VentaRequestDTO request = new VentaRequestDTO();
        Venta entity = new Venta();
        Venta saved = new Venta();
        VentaResponseDTO dto = new VentaResponseDTO();
        when(ventaMapper.toEntity(request)).thenReturn(entity);
        when(ventaService.save(entity)).thenReturn(saved);
        when(ventaMapper.toResponse(saved)).thenReturn(dto);

        assertThat(ventaController.guardarVenta(request)).isSameAs(dto);
    }
}
