package com.minimarket.hateoas;

import com.minimarket.controller.ProductoController;
import com.minimarket.controller.StockSucursalController;
import com.minimarket.controller.SucursalController;
import com.minimarket.dto.stocksucursal.StockSucursalResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class StockSucursalModelAssembler
        implements RepresentationModelAssembler<StockSucursalResponseDTO, EntityModel<StockSucursalResponseDTO>> {

    @Override
    public EntityModel<StockSucursalResponseDTO> toModel(StockSucursalResponseDTO stock) {
        EntityModel<StockSucursalResponseDTO> model = EntityModel.of(stock,
                linkTo(methodOn(StockSucursalController.class).obtenerStockPorId(stock.getId())).withSelfRel(),
                linkTo(methodOn(StockSucursalController.class).listarStockSucursal(Pageable.unpaged(), null))
                        .withRel("stock-sucursal"));

        if (stock.getProducto() != null && stock.getProducto().getId() != null) {
            model.add(linkTo(methodOn(ProductoController.class)
                    .obtenerProductoPorId(stock.getProducto().getId())).withRel("producto"));
        }
        if (stock.getSucursal() != null && stock.getSucursal().getId() != null) {
            model.add(linkTo(methodOn(SucursalController.class)
                    .obtenerSucursalPorId(stock.getSucursal().getId())).withRel("sucursal"));
        }
        return model;
    }
}
