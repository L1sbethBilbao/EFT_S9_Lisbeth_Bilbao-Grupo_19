package com.minimarket.hateoas;

import com.minimarket.dto.carrito.CarritoResponseDTO;
import com.minimarket.dto.categoria.CategoriaResponseDTO;
import com.minimarket.dto.inventario.InventarioResponseDTO;
import com.minimarket.dto.ordencompra.OrdenCompraResponseDTO;
import com.minimarket.dto.pedido.PedidoResponseDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.dto.promocion.PromocionResponseDTO;
import com.minimarket.dto.stocksucursal.StockSucursalResponseDTO;
import com.minimarket.dto.sucursal.SucursalResponseDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.assertj.core.api.Assertions.assertThat;

class HateoasModelAssemblerTest {

    private final PedidoModelAssembler pedidoAssembler = new PedidoModelAssembler();
    private final OrdenCompraModelAssembler ordenAssembler = new OrdenCompraModelAssembler();
    private final StockSucursalModelAssembler stockAssembler = new StockSucursalModelAssembler();
    private final SucursalModelAssembler sucursalAssembler = new SucursalModelAssembler();
    private final PromocionModelAssembler promocionAssembler = new PromocionModelAssembler();
    private final CategoriaModelAssembler categoriaAssembler = new CategoriaModelAssembler();
    private final ProductoModelAssembler productoAssembler = new ProductoModelAssembler();
    private final CarritoModelAssembler carritoAssembler = new CarritoModelAssembler();
    private final InventarioModelAssembler inventarioAssembler = new InventarioModelAssembler();

    @Test
    @DisplayName("GIVEN Pedido Con Sucursal WHEN To Model THEN Incluye Links Self Pedidos Y Sucursal")
    void givenPedidoConSucursal_whenToModel_thenIncluyeLinks() {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        SucursalResponseDTO sucursal = new SucursalResponseDTO();
        sucursal.setId(2L);
        dto.setSucursal(sucursal);

        EntityModel<PedidoResponseDTO> model = pedidoAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .contains("self", "pedidos", "sucursal");
    }

    @Test
    @DisplayName("GIVEN Pedido Sin Sucursal WHEN To Model THEN No Incluye Rel Sucursal")
    void givenPedidoSinSucursal_whenToModel_thenNoIncluyeRelSucursal() {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        dto.setSucursal(null);

        EntityModel<PedidoResponseDTO> model = pedidoAssembler.toModel(dto);

        assertThat(model.hasLink("sucursal")).isFalse();
        assertThat(model.hasLink("self")).isTrue();
    }

    @Test
    @DisplayName("GIVEN Pedido Sucursal Sin Id WHEN To Model THEN No Incluye Rel Sucursal")
    void givenPedidoSucursalSinId_whenToModel_thenNoIncluyeRelSucursal() {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(1L);
        dto.setSucursal(new SucursalResponseDTO());

        assertThat(pedidoAssembler.toModel(dto).hasLink("sucursal")).isFalse();
    }

    @Test
    @DisplayName("GIVEN OrdenCompra Con Sucursal WHEN To Model THEN Incluye Links")
    void givenOrdenCompraConSucursal_whenToModel_thenIncluyeLinks() {
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(5L);
        SucursalResponseDTO sucursal = new SucursalResponseDTO();
        sucursal.setId(1L);
        dto.setSucursal(sucursal);

        EntityModel<OrdenCompraResponseDTO> model = ordenAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .contains("self", "ordenes-compra", "sucursal");
    }

    @Test
    @DisplayName("GIVEN OrdenCompra Sin Sucursal WHEN To Model THEN Solo Links Base")
    void givenOrdenCompraSinSucursal_whenToModel_thenSoloLinksBase() {
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(5L);

        EntityModel<OrdenCompraResponseDTO> model = ordenAssembler.toModel(dto);

        assertThat(model.hasLink("self")).isTrue();
        assertThat(model.hasLink("sucursal")).isFalse();
    }

    @Test
    @DisplayName("GIVEN OrdenCompra Sucursal Sin Id WHEN To Model THEN No Rel Sucursal")
    void givenOrdenCompraSucursalSinId_whenToModel_thenNoRelSucursal() {
        OrdenCompraResponseDTO dto = new OrdenCompraResponseDTO();
        dto.setId(5L);
        dto.setSucursal(new SucursalResponseDTO());

        assertThat(ordenAssembler.toModel(dto).hasLink("sucursal")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Stock Con Producto Y Sucursal WHEN To Model THEN Incluye Relacionados")
    void givenStockConProductoYSucursal_whenToModel_thenIncluyeRelacionados() {
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(9L);
        ProductoResponseDTO producto = new ProductoResponseDTO();
        producto.setId(3L);
        dto.setProducto(producto);
        SucursalResponseDTO sucursal = new SucursalResponseDTO();
        sucursal.setId(1L);
        dto.setSucursal(sucursal);

        EntityModel<StockSucursalResponseDTO> model = stockAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .contains("self", "stock-sucursal", "producto", "sucursal");
    }

    @Test
    @DisplayName("GIVEN Stock Sin Relaciones WHEN To Model THEN Solo Links Base")
    void givenStockSinRelaciones_whenToModel_thenSoloLinksBase() {
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(9L);

        EntityModel<StockSucursalResponseDTO> model = stockAssembler.toModel(dto);

        assertThat(model.hasLink("producto")).isFalse();
        assertThat(model.hasLink("sucursal")).isFalse();
        assertThat(model.hasLink("self")).isTrue();
    }

    @Test
    @DisplayName("GIVEN Stock Relaciones Sin Id WHEN To Model THEN No Agrega Relacionados")
    void givenStockRelacionesSinId_whenToModel_thenNoAgregaRelacionados() {
        StockSucursalResponseDTO dto = new StockSucursalResponseDTO();
        dto.setId(9L);
        dto.setProducto(new ProductoResponseDTO());
        dto.setSucursal(new SucursalResponseDTO());

        EntityModel<StockSucursalResponseDTO> model = stockAssembler.toModel(dto);

        assertThat(model.hasLink("producto")).isFalse();
        assertThat(model.hasLink("sucursal")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Sucursal WHEN To Model THEN Incluye Self Y Coleccion")
    void givenSucursal_whenToModel_thenIncluyeSelfYColeccion() {
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(1L);

        EntityModel<SucursalResponseDTO> model = sucursalAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .containsExactlyInAnyOrder("self", "sucursales");
    }

    @Test
    @DisplayName("GIVEN Promocion WHEN To Model THEN Incluye Self Y Coleccion")
    void givenPromocion_whenToModel_thenIncluyeSelfYColeccion() {
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(4L);

        EntityModel<PromocionResponseDTO> model = promocionAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .containsExactlyInAnyOrder("self", "promociones");
    }

    @Test
    @DisplayName("GIVEN Categoria WHEN To Model THEN Incluye Self Y Coleccion")
    void givenCategoria_whenToModel_thenIncluyeSelfYColeccion() {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(2L);

        EntityModel<CategoriaResponseDTO> model = categoriaAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .containsExactlyInAnyOrder("self", "categorias");
    }

    @Test
    @DisplayName("GIVEN Producto Con Categoria WHEN To Model THEN Incluye Rel Categoria")
    void givenProductoConCategoria_whenToModel_thenIncluyeRelCategoria() {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);
        CategoriaResponseDTO categoria = new CategoriaResponseDTO();
        categoria.setId(2L);
        dto.setCategoria(categoria);

        EntityModel<ProductoResponseDTO> model = productoAssembler.toModel(dto);

        assertThat(model.hasLink("categoria")).isTrue();
        assertThat(model.hasLink("self")).isTrue();
        assertThat(model.hasLink("productos")).isTrue();
    }

    @Test
    @DisplayName("GIVEN Producto Sin Categoria WHEN To Model THEN No Rel Categoria")
    void givenProductoSinCategoria_whenToModel_thenNoRelCategoria() {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);

        assertThat(productoAssembler.toModel(dto).hasLink("categoria")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Producto Categoria Sin Id WHEN To Model THEN No Rel Categoria")
    void givenProductoCategoriaSinId_whenToModel_thenNoRelCategoria() {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(1L);
        dto.setCategoria(new CategoriaResponseDTO());

        assertThat(productoAssembler.toModel(dto).hasLink("categoria")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Carrito Con Usuario Y Producto WHEN To Model THEN Incluye Relacionados")
    void givenCarritoConUsuarioYProducto_whenToModel_thenIncluyeRelacionados() {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);
        UsuarioResponseDTO usuario = new UsuarioResponseDTO();
        usuario.setId(2L);
        dto.setUsuario(usuario);
        ProductoResponseDTO producto = new ProductoResponseDTO();
        producto.setId(3L);
        dto.setProducto(producto);

        EntityModel<CarritoResponseDTO> model = carritoAssembler.toModel(dto);

        assertThat(model.getLinks()).extracting(l -> l.getRel().value())
                .contains("self", "carrito", "usuario", "producto");
    }

    @Test
    @DisplayName("GIVEN Carrito Sin Relaciones WHEN To Model THEN Solo Links Base")
    void givenCarritoSinRelaciones_whenToModel_thenSoloLinksBase() {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);

        EntityModel<CarritoResponseDTO> model = carritoAssembler.toModel(dto);

        assertThat(model.hasLink("usuario")).isFalse();
        assertThat(model.hasLink("producto")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Carrito Relaciones Sin Id WHEN To Model THEN No Agrega Relacionados")
    void givenCarritoRelacionesSinId_whenToModel_thenNoAgregaRelacionados() {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(1L);
        dto.setUsuario(new UsuarioResponseDTO());
        dto.setProducto(new ProductoResponseDTO());

        EntityModel<CarritoResponseDTO> model = carritoAssembler.toModel(dto);

        assertThat(model.hasLink("usuario")).isFalse();
        assertThat(model.hasLink("producto")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Inventario Con Producto WHEN To Model THEN Incluye Rel Producto")
    void givenInventarioConProducto_whenToModel_thenIncluyeRelProducto() {
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);
        ProductoResponseDTO producto = new ProductoResponseDTO();
        producto.setId(4L);
        dto.setProducto(producto);

        EntityModel<InventarioResponseDTO> model = inventarioAssembler.toModel(dto);

        assertThat(model.hasLink("producto")).isTrue();
        assertThat(model.hasLink("self")).isTrue();
        assertThat(model.hasLink("inventario")).isTrue();
    }

    @Test
    @DisplayName("GIVEN Inventario Sin Producto WHEN To Model THEN No Rel Producto")
    void givenInventarioSinProducto_whenToModel_thenNoRelProducto() {
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);

        assertThat(inventarioAssembler.toModel(dto).hasLink("producto")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Inventario Producto Sin Id WHEN To Model THEN No Rel Producto")
    void givenInventarioProductoSinId_whenToModel_thenNoRelProducto() {
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setId(1L);
        dto.setProducto(new ProductoResponseDTO());

        assertThat(inventarioAssembler.toModel(dto).hasLink("producto")).isFalse();
    }
}
