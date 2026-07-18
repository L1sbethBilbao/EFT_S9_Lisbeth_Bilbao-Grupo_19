package com.minimarket.hateoas;

import com.minimarket.controller.OrdenCompraController;
import com.minimarket.controller.SucursalController;
import com.minimarket.dto.ordencompra.OrdenCompraResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrdenCompraModelAssembler
        implements RepresentationModelAssembler<OrdenCompraResponseDTO, EntityModel<OrdenCompraResponseDTO>> {

    @Override
    public EntityModel<OrdenCompraResponseDTO> toModel(OrdenCompraResponseDTO orden) {
        EntityModel<OrdenCompraResponseDTO> model = EntityModel.of(orden,
                linkTo(methodOn(OrdenCompraController.class).obtenerOrdenCompraPorId(orden.getId())).withSelfRel(),
                linkTo(methodOn(OrdenCompraController.class).listarOrdenesCompra(Pageable.unpaged(), null))
                        .withRel("ordenes-compra"));

        if (orden.getSucursal() != null && orden.getSucursal().getId() != null) {
            model.add(linkTo(methodOn(SucursalController.class)
                    .obtenerSucursalPorId(orden.getSucursal().getId())).withRel("sucursal"));
        }
        return model;
    }
}
