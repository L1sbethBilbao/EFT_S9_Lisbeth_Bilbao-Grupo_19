package com.minimarket.service.impl;

import com.minimarket.entity.Sucursal;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.support.TestFixtures;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucursalServiceImplTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private SucursalServiceImpl sucursalService;

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista Sanitizada")
    void givenDefaultContext_whenFindAll_thenRetornaListaSanitizada() {
        Sucursal sucursal = TestFixtures.sucursalProvidencia();
        when(sucursalRepository.findAll()).thenReturn(List.of(sucursal));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(sucursalService.findAll()).hasSize(1);
        verify(inputSanitizer).sanitize("MiniMarket Plus Providencia");
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Sucursal")
    void givenExiste_whenFindById_thenRetornaSucursal() {
        Sucursal sucursal = TestFixtures.sucursalProvidencia();
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(sucursalService.findById(1L)).isSameAs(sucursal);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(sucursalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(sucursalService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Sanitiza Y Persiste")
    void givenDefaultContext_whenSave_thenSanitizaYPersiste() {
        Sucursal sucursal = TestFixtures.sucursalProvidencia();
        when(sucursalRepository.save(sucursal)).thenReturn(sucursal);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(sucursalService.save(sucursal)).isSameAs(sucursal);
        verify(sucursalRepository).save(sucursal);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        sucursalService.deleteById(2L);

        verify(sucursalRepository).deleteById(2L);
    }
}
