package com.minimarket.controller;

import com.minimarket.dto.reporte.RotacionProductoResponseDTO;
import com.minimarket.service.ReporteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteController reporteController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Obtener Rotacion Productos THEN Retorna Lista")
    void givenDefaultContext_whenObtenerRotacionProductos_thenRetornaLista() {
        List<RotacionProductoResponseDTO> lista = List.of(new RotacionProductoResponseDTO());
        when(reporteService.obtenerRotacionProductos()).thenReturn(lista);

        assertThat(reporteController.obtenerRotacionProductos()).isSameAs(lista);
    }
}
