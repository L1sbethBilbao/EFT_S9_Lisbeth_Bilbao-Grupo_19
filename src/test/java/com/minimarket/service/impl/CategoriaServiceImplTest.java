package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
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
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista Sanitizada")
    void givenDefaultContext_whenFindAll_thenRetornaListaSanitizada() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Bebidas");
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(categoriaService.findAll()).hasSize(1);
        verify(inputSanitizer).sanitize("Bebidas");
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Categoria Sanitizada")
    void givenExiste_whenFindById_thenRetornaCategoriaSanitizada() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Lácteos");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(categoriaService.findById(1L)).isSameAs(categoria);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(categoriaService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Sanitiza Entrada Y Salida")
    void givenDefaultContext_whenSave_thenSanitizaEntradaYSalida() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Snacks");
        when(categoriaRepository.save(categoria)).thenReturn(categoria);
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        Categoria saved = categoriaService.save(categoria);

        assertThat(saved).isSameAs(categoria);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        categoriaService.deleteById(2L);

        verify(categoriaRepository).deleteById(2L);
    }
}
