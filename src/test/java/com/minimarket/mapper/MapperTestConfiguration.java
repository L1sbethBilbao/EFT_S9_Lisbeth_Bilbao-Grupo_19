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
        CarritoMapperImpl.class
})
public class MapperTestConfiguration {
}
