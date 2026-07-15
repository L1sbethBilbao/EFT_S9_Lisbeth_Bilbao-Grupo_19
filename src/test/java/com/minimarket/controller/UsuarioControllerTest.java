package com.minimarket.controller;

import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;
import com.minimarket.entity.Usuario;
import com.minimarket.hateoas.UsuarioModelAssembler;
import com.minimarket.mapper.UsuarioMapper;
import com.minimarket.service.UsuarioService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private UsuarioModelAssembler usuarioModelAssembler;

    @Mock
    private PagedResourcesAssembler<UsuarioResponseDTO> pagedAssembler;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Listar Usuarios THEN Retorna Pagina HATEOAS")
    void givenDefaultContext_whenListarUsuarios_thenRetornaPaginaHateoas() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Usuario> usuarios = List.of(new Usuario());
        Page<Usuario> entityPage = new PageImpl<>(usuarios, pageable, usuarios.size());
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(1L);
        PagedModel<EntityModel<UsuarioResponseDTO>> pagedModel = PagedModel.empty();

        when(usuarioService.findAll(pageable)).thenReturn(entityPage);
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(dto);
        when(pagedAssembler.toModel(any(Page.class), eq(usuarioModelAssembler))).thenReturn(pagedModel);

        assertThat(usuarioController.listarUsuarios(pageable, pagedAssembler)).isSameAs(pagedModel);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Obtener Usuario Por Id THEN Retorna Ok")
    void givenExiste_whenObtenerUsuarioPorId_thenRetornaOk() {
        Usuario usuario = new Usuario();
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(1L);
        EntityModel<UsuarioResponseDTO> model = EntityModel.of(dto);
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(dto);
        when(usuarioModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<UsuarioResponseDTO>> response = usuarioController.obtenerUsuarioPorId(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Obtener Usuario Por Id THEN Retorna404")
    void givenNoExiste_whenObtenerUsuarioPorId_thenRetorna404() {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<UsuarioResponseDTO>> response = usuarioController.obtenerUsuarioPorId(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Guardar Usuario THEN Retorna EntityModel")
    void givenDefaultContext_whenGuardarUsuario_thenRetornaEntityModel() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        Usuario saved = new Usuario();
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(1L);
        EntityModel<UsuarioResponseDTO> model = EntityModel.of(dto);
        when(usuarioService.saveFromDto(request)).thenReturn(saved);
        when(usuarioMapper.toResponse(saved)).thenReturn(dto);
        when(usuarioModelAssembler.toModel(dto)).thenReturn(model);

        assertThat(usuarioController.guardarUsuario(request)).isSameAs(model);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Actualizar Usuario THEN Retorna Ok")
    void givenExiste_whenActualizarUsuario_thenRetornaOk() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        Usuario usuario = new Usuario();
        Usuario saved = new Usuario();
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(1L);
        EntityModel<UsuarioResponseDTO> model = EntityModel.of(dto);
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.saveFromDto(request)).thenReturn(saved);
        when(usuarioMapper.toResponse(saved)).thenReturn(dto);
        when(usuarioModelAssembler.toModel(dto)).thenReturn(model);

        ResponseEntity<EntityModel<UsuarioResponseDTO>> response = usuarioController.actualizarUsuario(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
        assertThat(request.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Actualizar Usuario THEN Retorna404")
    void givenNoExiste_whenActualizarUsuario_thenRetorna404() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<EntityModel<UsuarioResponseDTO>> response = usuarioController.actualizarUsuario(99L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Eliminar Usuario THEN Retorna204")
    void givenExiste_whenEliminarUsuario_thenRetorna204() {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(new Usuario()));

        ResponseEntity<Void> response = usuarioController.eliminarUsuario(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(usuarioService).deleteById(1L);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Eliminar Usuario THEN Retorna404")
    void givenNoExiste_whenEliminarUsuario_thenRetorna404() {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = usuarioController.eliminarUsuario(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
