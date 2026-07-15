package com.minimarket.mapper;

import com.minimarket.dto.categoria.CategoriaRequestDTO;
import com.minimarket.dto.categoria.CategoriaResponseDTO;
import com.minimarket.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    CategoriaResponseDTO toResponse(Categoria categoria);

    List<CategoriaResponseDTO> toResponseList(List<Categoria> categorias);

    @Mapping(target = "productos", ignore = true)
    Categoria toEntity(CategoriaRequestDTO dto);
}
