package com.minimarket.hateoas;

import com.minimarket.controller.CarritoController;
import com.minimarket.controller.ProductoController;
import com.minimarket.controller.UsuarioController;
import com.minimarket.dto.carrito.CarritoResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CarritoModelAssembler
        implements RepresentationModelAssembler<CarritoResponseDTO, EntityModel<CarritoResponseDTO>> {

    @Override
    public EntityModel<CarritoResponseDTO> toModel(CarritoResponseDTO carrito) {
        EntityModel<CarritoResponseDTO> model = EntityModel.of(carrito,
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarrito(Pageable.unpaged(), null)).withRel("carrito"));

        if (carrito.getUsuario() != null && carrito.getUsuario().getId() != null) {
            model.add(linkTo(methodOn(UsuarioController.class)
                    .obtenerUsuarioPorId(carrito.getUsuario().getId())).withRel("usuario"));
        }
        if (carrito.getProducto() != null && carrito.getProducto().getId() != null) {
            model.add(linkTo(methodOn(ProductoController.class)
                    .obtenerProductoPorId(carrito.getProducto().getId())).withRel("producto"));
        }
        return model;
    }
}
