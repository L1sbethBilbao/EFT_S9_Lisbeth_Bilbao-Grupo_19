package com.minimarket.support;

import com.minimarket.dto.pedido.PedidoItemDTO;
import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.dto.venta.VentaItemDTO;
import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.Usuario;
import com.minimarket.security.constants.SecurityRoles;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TestFixtures {

    private static final Instant FECHA_FIJA = Instant.parse("2024-06-15T10:00:00Z");

    public static Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }

    public static Rol rolCliente() {
        Rol cliente = rol(SecurityRoles.CLIENTE);
        cliente.setId(1L);
        return cliente;
    }

    public static Rol rolEmpleado() {
        Rol empleado = rol(SecurityRoles.EMPLEADO);
        empleado.setId(2L);
        return empleado;
    }

    public static Usuario usuarioClienteCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente1");
        usuario.setNombre("Lisbeth");
        usuario.setApellido("Bilbao");
        usuario.setEmail("cliente1@minimarket.cl");
        usuario.setDireccion("Av. Principal 100");
        usuario.setRoles(new HashSet<>(Set.of(rolCliente())));
        return usuario;
    }

    public static Usuario usuarioEmpleadoCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setUsername("empleado1");
        usuario.setNombre("Carlos");
        usuario.setApellido("Cajero");
        usuario.setEmail("empleado1@minimarket.cl");
        usuario.setDireccion("Av. Principal 200");
        usuario.setRoles(new HashSet<>(Set.of(rolEmpleado())));
        return usuario;
    }

    public static Producto productoConStock(long id, String nombre, double precio, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        return producto;
    }

    public static Producto productoArrozConStock(int stock) {
        return productoConStock(10L, "Arroz", 1000.0, stock);
    }

    public static VentaRegistroDTO ventaRegistro(long usuarioId, long productoId, int cantidad) {
        VentaRegistroDTO dto = new VentaRegistroDTO();
        dto.setUsuarioId(usuarioId);
        VentaItemDTO item = new VentaItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        dto.setItems(List.of(item));
        return dto;
    }

    public static VentaRegistroDTO ventaRegistroMultiItem(long usuarioId, List<VentaItemDTO> items) {
        VentaRegistroDTO dto = new VentaRegistroDTO();
        dto.setUsuarioId(usuarioId);
        dto.setItems(items);
        return dto;
    }

    public static VentaItemDTO ventaItem(long productoId, int cantidad) {
        VentaItemDTO item = new VentaItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        return item;
    }

    public static Inventario inventarioEntrada(Producto producto, int cantidad) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(Date.from(FECHA_FIJA));
        return inventario;
    }

    public static Inventario inventarioSalida(Producto producto, int cantidad) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setTipoMovimiento("Salida");
        inventario.setFechaMovimiento(Date.from(FECHA_FIJA));
        return inventario;
    }

    public static Sucursal sucursal(long id, String nombre, String comuna) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        sucursal.setNombre(nombre);
        sucursal.setDireccion("Av. Test 100");
        sucursal.setComuna(comuna);
        sucursal.setActiva(true);
        return sucursal;
    }

    public static Sucursal sucursalProvidencia() {
        return sucursal(1L, "MiniMarket Plus Providencia", "Providencia");
    }

    public static StockSucursal stockSucursal(Producto producto, Sucursal sucursal, int cantidad, int minimo) {
        StockSucursal stock = new StockSucursal();
        stock.setId(1L);
        stock.setProducto(producto);
        stock.setSucursal(sucursal);
        stock.setCantidad(cantidad);
        stock.setStockMinimo(minimo);
        return stock;
    }

    public static PedidoRegistroDTO pedidoRegistro(long usuarioId, long sucursalId, long productoId, int cantidad) {
        PedidoRegistroDTO dto = new PedidoRegistroDTO();
        dto.setUsuarioId(usuarioId);
        dto.setSucursalId(sucursalId);
        dto.setTipoEntrega("RETIRO");
        PedidoItemDTO item = new PedidoItemDTO();
        item.setProductoId(productoId);
        item.setCantidad(cantidad);
        dto.setItems(List.of(item));
        return dto;
    }

    private TestFixtures() {
    }
}
