package com.minimarket.hateoas;

import com.minimarket.controller.CategoriaController;
import com.minimarket.dto.categoria.CategoriaResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<CategoriaResponseDTO, EntityModel<CategoriaResponseDTO>> {

    @Override
    public EntityModel<CategoriaResponseDTO> toModel(CategoriaResponseDTO categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(categoria.getId())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias(Pageable.unpaged(), null))
                        .withRel("categorias"));
    }
}
