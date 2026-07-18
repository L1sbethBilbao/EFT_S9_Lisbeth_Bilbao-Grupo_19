package com.minimarket.hateoas;

import com.minimarket.controller.PromocionController;
import com.minimarket.dto.promocion.PromocionResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PromocionModelAssembler
        implements RepresentationModelAssembler<PromocionResponseDTO, EntityModel<PromocionResponseDTO>> {

    @Override
    public EntityModel<PromocionResponseDTO> toModel(PromocionResponseDTO promocion) {
        return EntityModel.of(promocion,
                linkTo(methodOn(PromocionController.class).obtenerPromocionPorId(promocion.getId())).withSelfRel(),
                linkTo(methodOn(PromocionController.class).listarPromociones(Pageable.unpaged(), null))
                        .withRel("promociones"));
    }
}
