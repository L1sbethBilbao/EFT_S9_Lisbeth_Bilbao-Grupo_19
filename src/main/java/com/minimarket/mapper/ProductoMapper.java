package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface ProductoMapper {

    ProductoResponseDTO toResponse(Producto producto);

    List<ProductoResponseDTO> toResponseList(List<Producto> productos);

    @Mapping(target = "categoria", source = "categoria", qualifiedByName = "idRefToCategoria")
    Producto toEntity(ProductoRequestDTO dto);

    @Named("idRefToCategoria")
    default Categoria idRefToCategoria(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Categoria categoria = new Categoria();
        categoria.setId(ref.getId());
        return categoria;
    }
}
