package com.minimarket.hateoas;

import com.minimarket.controller.CategoriaController;
import com.minimarket.controller.ProductoController;
import com.minimarket.dto.producto.ProductoResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler
        implements RepresentationModelAssembler<ProductoResponseDTO, EntityModel<ProductoResponseDTO>> {

    @Override
    public EntityModel<ProductoResponseDTO> toModel(ProductoResponseDTO producto) {
        EntityModel<ProductoResponseDTO> model = EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos(Pageable.unpaged(), null)).withRel("productos"));

        if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
            model.add(linkTo(methodOn(CategoriaController.class)
                    .obtenerCategoriaPorId(producto.getCategoria().getId())).withRel("categoria"));
        }
        return model;
    }
}
