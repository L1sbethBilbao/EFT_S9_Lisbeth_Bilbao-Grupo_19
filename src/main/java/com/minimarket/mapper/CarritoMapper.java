package com.minimarket.mapper;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.carrito.CarritoResponseDTO;
import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, ProductoMapper.class})
public interface CarritoMapper {

    CarritoResponseDTO toResponse(Carrito carrito);

    List<CarritoResponseDTO> toResponseList(List<Carrito> carritos);

    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "idRefToUsuario")
    @Mapping(target = "producto", source = "producto", qualifiedByName = "idRefToProducto")
    Carrito toEntity(CarritoRequestDTO dto);

    @Named("idRefToUsuario")
    default Usuario idRefToUsuario(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setId(ref.getId());
        return usuario;
    }

    @Named("idRefToProducto")
    default Producto idRefToProducto(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Producto producto = new Producto();
        producto.setId(ref.getId());
        return producto;
    }
}
