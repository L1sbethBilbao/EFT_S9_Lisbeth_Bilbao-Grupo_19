package com.minimarket.mapper;

import com.minimarket.dto.usuario.RolDTO;
import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToDto")
    UsuarioResponseDTO toResponse(Usuario usuario);

    List<UsuarioResponseDTO> toResponseList(List<Usuario> usuarios);

    @Mapping(target = "password", source = "password")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "mfaEnabled", ignore = true)
    @Mapping(target = "totpSecret", ignore = true)
    @Mapping(target = "mfaEnrolledAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "anonymized", ignore = true)
    @Mapping(target = "retentionExcluded", ignore = true)
    Usuario toEntity(UsuarioRequestDTO dto);

    @Named("rolesToDto")
    default List<RolDTO> rolesToDto(Set<Rol> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(r -> new RolDTO(r.getId(), r.getNombre()))
                .collect(Collectors.toList());
    }
}
