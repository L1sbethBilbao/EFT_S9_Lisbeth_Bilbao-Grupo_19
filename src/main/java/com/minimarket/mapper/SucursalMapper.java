package com.minimarket.mapper;

import com.minimarket.dto.sucursal.SucursalRequestDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;
import com.minimarket.entity.Sucursal;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SucursalMapper {

    SucursalResponseDTO toResponse(Sucursal sucursal);

    List<SucursalResponseDTO> toResponseList(List<Sucursal> sucursales);

    Sucursal toEntity(SucursalRequestDTO dto);
}
