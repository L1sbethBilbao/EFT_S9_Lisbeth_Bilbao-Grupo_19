package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.venta.VentaRequestDTO;
import com.minimarket.dto.venta.VentaResponseDTO;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = UsuarioMapper.class)
public interface VentaMapper {

    VentaResponseDTO toResponse(Venta venta);

    List<VentaResponseDTO> toResponseList(List<Venta> ventas);

    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "idRefToUsuario")
    @Mapping(target = "detalles", ignore = true)
    Venta toEntity(VentaRequestDTO dto);

    @Named("idRefToUsuario")
    default Usuario idRefToUsuario(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setId(ref.getId());
        return usuario;
    }
}
