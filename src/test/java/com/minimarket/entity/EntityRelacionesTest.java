package com.minimarket.entity;

import com.minimarket.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntityRelacionesTest {

    @Test
    @DisplayName("GIVEN Usuario Y Carrito WHEN Asociar THEN Mantiene Relacion Bidireccional")
    void givenUsuarioYCarrito_whenAsociar_thenMantieneRelacionBidireccional() {
        Usuario usuario = TestFixtures.usuarioClienteCompleto();
        Producto producto = TestFixtures.productoArrozConStock(5);

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(2);

        assertThat(carrito.getUsuario().getUsername()).isEqualTo("cliente1");
        assertThat(carrito.getProducto().getNombre()).isEqualTo("Arroz");
        assertThat(carrito.getCantidad()).isEqualTo(2);
    }

    @Test
    @DisplayName("GIVEN Producto E Inventario WHEN Asociar THEN Movimiento Referencia Producto")
    void givenProductoEInventario_whenAsociar_thenMovimientoReferenciaProducto() {
        Producto producto = TestFixtures.productoArrozConStock(10);
        Inventario inventario = TestFixtures.inventarioEntrada(producto, 5);

        assertThat(inventario.getProducto()).isSameAs(producto);
        assertThat(inventario.getTipoMovimiento()).isEqualTo("Entrada");
        assertThat(inventario.getFechaMovimiento()).isNotNull();
    }

    @Test
    @DisplayName("GIVEN Venta Y Detalles WHEN Asociar THEN Detalle Referencia Venta")
    void givenVentaYDetalles_whenAsociar_thenDetalleReferenciaVenta() {
        Usuario usuario = TestFixtures.usuarioEmpleadoCompleto();
        Producto producto = TestFixtures.productoArrozConStock(3);

        Venta venta = new Venta();
        venta.setId(10L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setTotal(2000.0);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecio(1000.0);
        detalle.setVenta(venta);

        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalle);
        venta.setDetalles(detalles);

        assertThat(venta.getDetalles()).hasSize(1);
        assertThat(venta.getDetalles().get(0).getVenta()).isSameAs(venta);
        assertThat(venta.getUsuario().getUsername()).isEqualTo("empleado1");
    }

    @Test
    @DisplayName("GIVEN Entidades WHEN Getters Setters THEN Conservan Valores")
    void givenEntidades_whenGettersSetters_thenConservanValores() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");

        Producto producto = new Producto();
        producto.setId(5L);
        producto.setNombre("Aceite");
        producto.setPrecio(2500.0);
        producto.setStock(15);
        producto.setDescripcion("1L");
        producto.setCategoria(categoria);

        assertThat(producto.getCategoria().getNombre()).isEqualTo("Abarrotes");
        assertThat(producto.getDescripcion()).isEqualTo("1L");
    }
}
