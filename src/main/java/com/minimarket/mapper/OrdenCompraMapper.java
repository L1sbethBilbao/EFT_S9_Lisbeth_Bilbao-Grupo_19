package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.ordencompra.DetalleOrdenCompraResponseDTO;
import com.minimarket.dto.ordencompra.OrdenCompraResponseDTO;
import com.minimarket.entity.DetalleOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.Sucursal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SucursalMapper.class, ProductoMapper.class})
public interface OrdenCompraMapper {

    OrdenCompraResponseDTO toResponse(OrdenCompra ordenCompra);

    List<OrdenCompraResponseDTO> toResponseList(List<OrdenCompra> ordenes);

    DetalleOrdenCompraResponseDTO toDetalleResponse(DetalleOrdenCompra detalle);

    @Mapping(target = "sucursal", source = "sucursal", qualifiedByName = "idRefToSucursal")
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    OrdenCompra toEntity(com.minimarket.dto.ordencompra.OrdenCompraRequestDTO dto);

    @Named("idRefToSucursal")
    default Sucursal idRefToSucursal(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Sucursal sucursal = new Sucursal();
        sucursal.setId(ref.getId());
        return sucursal;
    }
}
