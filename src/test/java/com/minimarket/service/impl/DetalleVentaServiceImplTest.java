package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.repository.DetalleVentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetalleVentaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private DetalleVentaServiceImpl detalleVentaService;

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista")
    void givenDefaultContext_whenFindAll_thenRetornaLista() {
        DetalleVenta detalle = new DetalleVenta();
        when(detalleVentaRepository.findAll()).thenReturn(List.of(detalle));

        assertThat(detalleVentaService.findAll()).containsExactly(detalle);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Detalle")
    void givenExiste_whenFindById_thenRetornaDetalle() {
        DetalleVenta detalle = new DetalleVenta();
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(detalle));

        assertThat(detalleVentaService.findById(1L)).isSameAs(detalle);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(detalleVentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(detalleVentaService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Guarda Detalle")
    void givenDefaultContext_whenSave_thenGuardaDetalle() {
        DetalleVenta detalle = new DetalleVenta();
        when(detalleVentaRepository.save(detalle)).thenReturn(detalle);

        assertThat(detalleVentaService.save(detalle)).isSameAs(detalle);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        detalleVentaService.deleteById(2L);

        verify(detalleVentaRepository).deleteById(2L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Venta Id THEN Retorna Detalles")
    void givenDefaultContext_whenFindByVentaId_thenRetornaDetalles() {
        DetalleVenta detalle = new DetalleVenta();
        when(detalleVentaRepository.findByVentaId(7L)).thenReturn(List.of(detalle));

        assertThat(detalleVentaService.findByVentaId(7L)).containsExactly(detalle);
    }
}
