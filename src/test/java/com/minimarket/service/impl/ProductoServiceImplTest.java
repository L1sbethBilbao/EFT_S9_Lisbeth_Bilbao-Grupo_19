package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista Sanitizada")
    void givenDefaultContext_whenFindAll_thenRetornaListaSanitizada() {
        Producto producto = new Producto();
        producto.setNombre("Arroz");
        producto.setDescripcion("1 kg");
        when(productoRepository.findAll()).thenReturn(List.of(producto));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(productoService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Producto Sanitizado")
    void givenExiste_whenFindById_thenRetornaProductoSanitizado() {
        Producto producto = new Producto();
        producto.setNombre("Aceite");
        producto.setDescripcion("900 ml");
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(productoService.findById(1L)).isSameAs(producto);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(productoService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Sanitiza Entrada Y Salida")
    void givenDefaultContext_whenSave_thenSanitizaEntradaYSalida() {
        Producto producto = new Producto();
        producto.setNombre("Pan");
        producto.setDescripcion("Integral");
        when(productoRepository.save(producto)).thenReturn(producto);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(productoService.save(producto)).isSameAs(producto);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        productoService.deleteById(3L);

        verify(productoRepository).deleteById(3L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Categoria Id THEN Retorna Productos Sanitizados")
    void givenDefaultContext_whenFindByCategoriaId_thenRetornaProductosSanitizados() {
        Producto producto = new Producto();
        producto.setNombre("Leche");
        producto.setDescripcion("Entera");
        when(productoRepository.findByCategoriaId(2L)).thenReturn(List.of(producto));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(productoService.findByCategoriaId(2L)).hasSize(1);
    }
}
