package com.minimarket.hateoas;

import com.minimarket.controller.VentaController;
import com.minimarket.dto.venta.VentaResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VentaModelAssembler
        implements RepresentationModelAssembler<VentaResponseDTO, EntityModel<VentaResponseDTO>> {

    @Override
    public EntityModel<VentaResponseDTO> toModel(VentaResponseDTO venta) {
        return EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas(Pageable.unpaged(), null)).withRel("ventas"));
    }
}
