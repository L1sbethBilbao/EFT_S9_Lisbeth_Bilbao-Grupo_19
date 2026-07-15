package com.minimarket.controller;

import com.minimarket.dto.categoria.CategoriaRequestDTO;
import com.minimarket.dto.categoria.CategoriaResponseDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.mapper.CategoriaMapper;
import com.minimarket.service.CategoriaService;
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
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private CategoriaMapper categoriaMapper;

    @InjectMocks
    private CategoriaController categoriaController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Categorias THEN Retorna Lista Mapeada")
    void givenDefaultContext_whenListarCategorias_thenRetornaListaMapeada() {
        List<Categoria> categorias = List.of(new Categoria());
        List<CategoriaResponseDTO> dtos = List.of(new CategoriaResponseDTO());
        when(categoriaService.findAll()).thenReturn(categorias);
        when(categoriaMapper.toResponseList(categorias)).thenReturn(dtos);

        assertThat(categoriaController.listarCategorias()).isSameAs(dtos);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Categoria Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerCategoriaPorId_thenRetornaOk() {
        Categoria categoria = new Categoria();
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(categoriaMapper.toResponse(categoria)).thenReturn(dto);

        ResponseEntity<CategoriaResponseDTO> response = categoriaController.obtenerCategoriaPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Categoria Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerCategoriaPorId_thenRetorna404() {
        when(categoriaService.findById(99L)).thenReturn(null);

        ResponseEntity<CategoriaResponseDTO> response = categoriaController.obtenerCategoriaPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Categoria THEN Retorna Dto")
    void givenDefaultContext_whenGuardarCategoria_thenRetornaDto() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        Categoria entity = new Categoria();
        Categoria saved = new Categoria();
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        when(categoriaMapper.toEntity(request)).thenReturn(entity);
        when(categoriaService.save(entity)).thenReturn(saved);
        when(categoriaMapper.toResponse(saved)).thenReturn(dto);

        assertThat(categoriaController.guardarCategoria(request)).isSameAs(dto);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Categoria THEN Retorna Ok")
    void givenExiste_whenActualizarCategoria_thenRetornaOk() {
        CategoriaRequestDTO request = new CategoriaRequestDTO();
        Categoria saved = new Categoria();
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        when(categoriaService.findById(1L)).thenReturn(new Categoria());
        when(categoriaMapper.toEntity(request)).thenReturn(new Categoria());
        when(categoriaService.save(any(Categoria.class))).thenReturn(saved);
        when(categoriaMapper.toResponse(saved)).thenReturn(dto);

        ResponseEntity<CategoriaResponseDTO> response = categoriaController.actualizarCategoria(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dto);
        assertThat(request.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Categoria THEN Retorna404")
    void givenNoExiste_whenActualizarCategoria_thenRetorna404() {
        when(categoriaService.findById(99L)).thenReturn(null);

        ResponseEntity<CategoriaResponseDTO> response =
                categoriaController.actualizarCategoria(99L, new CategoriaRequestDTO());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Categoria THEN Retorna204")
    void givenExiste_whenEliminarCategoria_thenRetorna204() {
        when(categoriaService.findById(1L)).thenReturn(new Categoria());

        ResponseEntity<Void> response = categoriaController.eliminarCategoria(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoriaService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Categoria THEN Retorna404")
    void givenNoExiste_whenEliminarCategoria_thenRetorna404() {
        when(categoriaService.findById(99L)).thenReturn(null);

        ResponseEntity<Void> response = categoriaController.eliminarCategoria(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
