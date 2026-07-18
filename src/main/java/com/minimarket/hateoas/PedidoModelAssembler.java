package com.minimarket.hateoas;

import com.minimarket.controller.PedidoController;
import com.minimarket.controller.SucursalController;
import com.minimarket.dto.pedido.PedidoResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PedidoModelAssembler
        implements RepresentationModelAssembler<PedidoResponseDTO, EntityModel<PedidoResponseDTO>> {

    @Override
    public EntityModel<PedidoResponseDTO> toModel(PedidoResponseDTO pedido) {
        EntityModel<PedidoResponseDTO> model = EntityModel.of(pedido,
                linkTo(methodOn(PedidoController.class).obtenerPedidoPorId(pedido.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listarPedidos(Pageable.unpaged(), null)).withRel("pedidos"));

        if (pedido.getSucursal() != null && pedido.getSucursal().getId() != null) {
            model.add(linkTo(methodOn(SucursalController.class)
                    .obtenerSucursalPorId(pedido.getSucursal().getId())).withRel("sucursal"));
        }
        return model;
    }
}
