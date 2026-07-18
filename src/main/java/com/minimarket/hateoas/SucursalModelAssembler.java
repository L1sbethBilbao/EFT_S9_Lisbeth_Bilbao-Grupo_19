package com.minimarket.hateoas;

import com.minimarket.controller.SucursalController;
import com.minimarket.dto.sucursal.SucursalResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SucursalModelAssembler
        implements RepresentationModelAssembler<SucursalResponseDTO, EntityModel<SucursalResponseDTO>> {

    @Override
    public EntityModel<SucursalResponseDTO> toModel(SucursalResponseDTO sucursal) {
        return EntityModel.of(sucursal,
                linkTo(methodOn(SucursalController.class).obtenerSucursalPorId(sucursal.getId())).withSelfRel(),
                linkTo(methodOn(SucursalController.class).listarSucursales(Pageable.unpaged(), null))
                        .withRel("sucursales"));
    }
}
