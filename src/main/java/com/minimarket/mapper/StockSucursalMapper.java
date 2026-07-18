package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.stocksucursal.StockSucursalRequestDTO;
import com.minimarket.dto.stocksucursal.StockSucursalResponseDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductoMapper.class, SucursalMapper.class})
public interface StockSucursalMapper {

    StockSucursalResponseDTO toResponse(StockSucursal stockSucursal);

    List<StockSucursalResponseDTO> toResponseList(List<StockSucursal> stocks);

    @Mapping(target = "producto", source = "producto", qualifiedByName = "idRefToProducto")
    @Mapping(target = "sucursal", source = "sucursal", qualifiedByName = "idRefToSucursal")
    StockSucursal toEntity(StockSucursalRequestDTO dto);

    @Named("idRefToProducto")
    default Producto idRefToProducto(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Producto producto = new Producto();
        producto.setId(ref.getId());
        return producto;
    }

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
