package com.minimarket.mapper;

import com.minimarket.dto.pedido.DetallePedidoResponseDTO;
import com.minimarket.dto.pedido.PedidoResponseDTO;
import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.Pedido;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UsuarioMapper.class, SucursalMapper.class, ProductoMapper.class})
public interface PedidoMapper {

    PedidoResponseDTO toResponse(Pedido pedido);

    List<PedidoResponseDTO> toResponseList(List<Pedido> pedidos);

    DetallePedidoResponseDTO toDetalleResponse(DetallePedido detalle);
}
