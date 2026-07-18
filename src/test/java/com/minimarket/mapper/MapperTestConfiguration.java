package com.minimarket.mapper;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import({
        CategoriaMapperImpl.class,
        UsuarioMapperImpl.class,
        ProductoMapperImpl.class,
        VentaMapperImpl.class,
        InventarioMapperImpl.class,
        DetalleVentaMapperImpl.class,
        CarritoMapperImpl.class,
        SucursalMapperImpl.class,
        StockSucursalMapperImpl.class,
        PromocionMapperImpl.class,
        PedidoMapperImpl.class,
        OrdenCompraMapperImpl.class
})
public class MapperTestConfiguration {
}
