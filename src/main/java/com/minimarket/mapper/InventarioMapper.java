package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.dto.inventario.InventarioResponseDTO;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProductoMapper.class)
public interface InventarioMapper {

    InventarioResponseDTO toResponse(Inventario inventario);

    List<InventarioResponseDTO> toResponseList(List<Inventario> inventarios);

    @Mapping(target = "producto", source = "producto", qualifiedByName = "idRefToProducto")
    Inventario toEntity(InventarioRequestDTO dto);

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
