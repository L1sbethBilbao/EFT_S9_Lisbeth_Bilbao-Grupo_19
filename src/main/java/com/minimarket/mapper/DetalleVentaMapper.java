package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.detalleventa.DetalleVentaRequestDTO;
import com.minimarket.dto.detalleventa.DetalleVentaResponseDTO;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProductoMapper.class)
public interface DetalleVentaMapper {

    @Mapping(target = "venta", source = "venta", qualifiedByName = "ventaToIdRef")
    DetalleVentaResponseDTO toResponse(DetalleVenta detalleVenta);

    List<DetalleVentaResponseDTO> toResponseList(List<DetalleVenta> detalleVentas);

    @Mapping(target = "venta", source = "venta", qualifiedByName = "idRefToVenta")
    @Mapping(target = "producto", source = "producto", qualifiedByName = "idRefToProducto")
    DetalleVenta toEntity(DetalleVentaRequestDTO dto);

    @Named("ventaToIdRef")
    default IdRefDTO ventaToIdRef(Venta venta) {
        if (venta == null) {
            return null;
        }
        return new IdRefDTO(venta.getId());
    }

    @Named("idRefToVenta")
    default Venta idRefToVenta(IdRefDTO ref) {
        if (ref == null || ref.getId() == null) {
            return null;
        }
        Venta venta = new Venta();
        venta.setId(ref.getId());
        return venta;
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
