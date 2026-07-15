package com.minimarket.controller;

import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;
import com.minimarket.hateoas.UsuarioModelAssembler;
import com.minimarket.mapper.UsuarioMapper;
import com.minimarket.security.audit.AuditAction;
import com.minimarket.security.audit.Audited;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Usuarios", description = "Administración de usuarios. Solo rol GERENTE")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private UsuarioModelAssembler usuarioModelAssembler;

    @Operation(summary = "Listar usuarios",
            description = "Lista paginada HAL con _embedded, page y links de paginación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuarios"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @GetMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.LIST)
    public PagedModel<EntityModel<UsuarioResponseDTO>> listarUsuarios(
            @ParameterObject Pageable pageable,
            PagedResourcesAssembler<UsuarioResponseDTO> pagedAssembler) {
        Page<UsuarioResponseDTO> page = usuarioService.findAll(pageable).map(usuarioMapper::toResponse);
        return pagedAssembler.toModel(page, usuarioModelAssembler);
    }

    @Operation(summary = "Obtener usuario por ID",
            description = "Respuesta HAL con _links (self, usuarios).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.READ)
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> obtenerUsuarioPorId(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        Optional<com.minimarket.entity.Usuario> usuario = usuarioService.findById(id);
        return usuario.map(u -> ResponseEntity.ok(usuarioModelAssembler.toModel(usuarioMapper.toResponse(u))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear usuario", description = "Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content)
    })
    @PostMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.CREATE)
    public EntityModel<UsuarioResponseDTO> guardarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Usuario a crear",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UsuarioRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Usuario cliente",
                                    value = """
                                            {
                                              "username": "cliente_nuevo",
                                              "password": "cliente123",
                                              "nombre": "Ana",
                                              "apellido": "Pérez",
                                              "email": "ana@example.com",
                                              "direccion": "Calle 123",
                                              "roleNames": ["CLIENTE"]
                                            }
                                            """)))
            @Valid @RequestBody UsuarioRequestDTO usuarioDto) {
        UsuarioResponseDTO response = usuarioMapper.toResponse(usuarioService.saveFromDto(usuarioDto));
        return usuarioModelAssembler.toModel(response);
    }

    @Operation(summary = "Actualizar usuario", description = "Respuesta HAL con _links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.UPDATE)
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> actualizarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del usuario",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UsuarioRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "Actualizar dirección",
                                    value = """
                                            {
                                              "username": "cliente1",
                                              "nombre": "Cliente",
                                              "apellido": "Demo",
                                              "email": "cliente1@minimarket.cl",
                                              "direccion": "Av. Principal 456",
                                              "roleNames": ["CLIENTE"]
                                            }
                                            """)))
            @Valid @RequestBody UsuarioRequestDTO usuarioDto) {
        Optional<com.minimarket.entity.Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuarioDto.setId(id);
            UsuarioResponseDTO response = usuarioMapper.toResponse(usuarioService.saveFromDto(usuarioDto));
            return ResponseEntity.ok(usuarioModelAssembler.toModel(response));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere GERENTE)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.DELETE)
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario", example = "1") @PathVariable Long id) {
        Optional<com.minimarket.entity.Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
