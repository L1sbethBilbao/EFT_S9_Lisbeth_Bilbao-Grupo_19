package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.Sucursal;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceImplTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private PromocionServiceImpl promocionService;

    @Test
    @DisplayName("GIVEN Promocion Activa WHEN Aplicar Descuento THEN Reduce Precio")
    void givenPromocionActiva_whenAplicarDescuento_thenReducePrecio() {
        Promocion promocion = promocionActiva(10.0, 1L, null);
        when(promocionRepository.findActivasEnFecha(any(Date.class))).thenReturn(List.of(promocion));

        double precio = promocionService.aplicarDescuento(1L, 2L, 1000.0);

        assertThat(precio).isEqualTo(900.0);
    }

    @Test
    @DisplayName("GIVEN Promocion Otra Sucursal WHEN Aplicar Descuento THEN No Aplica")
    void givenPromocionOtraSucursal_whenAplicarDescuento_thenNoAplica() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(99L);
        Promocion promocion = promocionActiva(20.0, 1L, sucursal);
        when(promocionRepository.findActivasEnFecha(any(Date.class))).thenReturn(List.of(promocion));

        double precio = promocionService.aplicarDescuento(1L, 2L, 1000.0);

        assertThat(precio).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find Activas THEN Retorna Lista")
    void givenDefaultContext_whenFindActivas_thenRetornaLista() {
        Promocion promocion = promocionActiva(5.0, null, null);
        when(promocionRepository.findActivasEnFecha(any(Date.class))).thenReturn(List.of(promocion));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(promocionService.findActivas()).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Sanitiza Y Persiste")
    void givenDefaultContext_whenSave_thenSanitizaYPersiste() {
        Promocion promocion = promocionActiva(5.0, null, null);
        when(promocionRepository.save(promocion)).thenReturn(promocion);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(promocionService.save(promocion)).isSameAs(promocion);
        verify(promocionRepository).save(promocion);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Promocion")
    void givenExiste_whenFindById_thenRetornaPromocion() {
        Promocion promocion = promocionActiva(5.0, null, null);
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(promocionService.findById(1L)).isSameAs(promocion);
    }

    private Promocion promocionActiva(double descuento, Long productoId, Sucursal sucursal) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date inicio = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date fin = cal.getTime();

        Promocion promocion = new Promocion();
        promocion.setId(1L);
        promocion.setNombre("Test");
        promocion.setDescuentoPorcentaje(descuento);
        promocion.setFechaInicio(inicio);
        promocion.setFechaFin(fin);
        promocion.setActiva(true);
        if (productoId != null) {
            Producto producto = new Producto();
            producto.setId(productoId);
            promocion.setProducto(producto);
        }
        promocion.setSucursal(sucursal);
        return promocion;
    }
}
