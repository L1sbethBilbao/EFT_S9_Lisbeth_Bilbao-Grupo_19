package com.minimarket.mapper;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.ordencompra.OrdenCompraRequestDTO;
import com.minimarket.dto.promocion.PromocionRequestDTO;
import com.minimarket.dto.stocksucursal.StockSucursalRequestDTO;
import com.minimarket.dto.sucursal.SucursalRequestDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.DetalleOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MapperTestConfiguration.class)
class Semana9MapperTest {

    @Autowired
    private SucursalMapper sucursalMapper;

    @Autowired
    private StockSucursalMapper stockSucursalMapper;

    @Autowired
    private PromocionMapper promocionMapper;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private OrdenCompraMapper ordenCompraMapper;

    @Test
    @DisplayName("GIVEN Sucursal WHEN To Response THEN Mapea Campos")
    void givenSucursal_whenToResponse_thenMapeaCampos() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Providencia");
        sucursal.setComuna("Providencia");
        sucursal.setActiva(true);

        var dto = sucursalMapper.toResponse(sucursal);

        assertThat(dto.getNombre()).isEqualTo("Providencia");
        assertThat(sucursalMapper.toResponse(null)).isNull();
        assertThat(sucursalMapper.toResponseList(List.of(sucursal))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Sucursal Request WHEN To Entity THEN Mapea")
    void givenSucursalRequest_whenToEntity_thenMapea() {
        SucursalRequestDTO request = new SucursalRequestDTO();
        request.setNombre("Maipú");
        request.setDireccion("Av. 1");
        request.setComuna("Maipú");

        Sucursal entity = sucursalMapper.toEntity(request);

        assertThat(entity.getNombre()).isEqualTo("Maipú");
        assertThat(sucursalMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Stock Sucursal WHEN To Response THEN Mapea Relaciones")
    void givenStockSucursal_whenToResponse_thenMapeaRelaciones() {
        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Arroz");
        producto.setPrecio(1000.0);
        producto.setStock(10);
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");
        producto.setCategoria(categoria);

        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Centro");

        StockSucursal stock = new StockSucursal();
        stock.setId(5L);
        stock.setProducto(producto);
        stock.setSucursal(sucursal);
        stock.setCantidad(8);
        stock.setStockMinimo(3);

        var dto = stockSucursalMapper.toResponse(stock);

        assertThat(dto.getCantidad()).isEqualTo(8);
        assertThat(dto.getProducto().getNombre()).isEqualTo("Arroz");
        assertThat(stockSucursalMapper.toResponseList(List.of(stock))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Stock Request WHEN To Entity THEN Resuelve Referencias")
    void givenStockRequest_whenToEntity_thenResuelveReferencias() {
        StockSucursalRequestDTO request = new StockSucursalRequestDTO();
        request.setProducto(new IdRefDTO(2L));
        request.setSucursal(new IdRefDTO(1L));
        request.setCantidad(10);
        request.setStockMinimo(5);

        StockSucursal entity = stockSucursalMapper.toEntity(request);

        assertThat(entity.getProducto().getId()).isEqualTo(2L);
        assertThat(entity.getSucursal().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GIVEN Promocion WHEN To Response THEN Mapea")
    void givenPromocion_whenToResponse_thenMapea() {
        Promocion promocion = new Promocion();
        promocion.setId(1L);
        promocion.setNombre("Oferta");
        promocion.setDescuentoPorcentaje(10.0);
        promocion.setFechaInicio(new Date());
        promocion.setFechaFin(new Date());
        promocion.setActiva(true);

        assertThat(promocionMapper.toResponse(promocion).getNombre()).isEqualTo("Oferta");
        assertThat(promocionMapper.toResponseList(List.of(promocion))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Promocion Request WHEN To Entity THEN Mapea Referencias")
    void givenPromocionRequest_whenToEntity_thenMapeaReferencias() {
        PromocionRequestDTO request = new PromocionRequestDTO();
        request.setNombre("Descuento");
        request.setDescuentoPorcentaje(15.0);
        request.setFechaInicio(new Date());
        request.setFechaFin(new Date());
        request.setProducto(new IdRefDTO(1L));
        request.setSucursal(new IdRefDTO(2L));

        Promocion entity = promocionMapper.toEntity(request);

        assertThat(entity.getProducto().getId()).isEqualTo(1L);
        assertThat(entity.getSucursal().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("GIVEN Pedido WHEN To Response THEN Mapea")
    void givenPedido_whenToResponse_thenMapea() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setTotal(1000.0);

        assertThat(pedidoMapper.toResponse(pedido).getTotal()).isEqualTo(1000.0);
        assertThat(pedidoMapper.toResponseList(List.of(pedido))).hasSize(1);
        assertThat(pedidoMapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN OrdenCompra WHEN To Response THEN Mapea Campos Y Detalles")
    void givenOrdenCompra_whenToResponse_thenMapeaCamposYDetalles() {
        OrdenCompra orden = ordenCompraDemo();

        var dto = ordenCompraMapper.toResponse(orden);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getProveedor()).isEqualTo("Proveedor Demo");
        assertThat(dto.getEstado()).isEqualTo("PENDIENTE");
        assertThat(dto.getSucursal().getNombre()).isEqualTo("Providencia");
        assertThat(dto.getDetalles()).hasSize(1);
        assertThat(dto.getDetalles().get(0).getCantidad()).isEqualTo(5);
        assertThat(dto.getDetalles().get(0).getCostoUnitario()).isEqualTo(500.0);
        assertThat(dto.getDetalles().get(0).getProducto().getNombre()).isEqualTo("Arroz");
    }

    @Test
    @DisplayName("GIVEN Null WHEN OrdenCompra To Response THEN Retorna Null")
    void givenNull_whenOrdenCompraToResponse_thenRetornaNull() {
        assertThat(ordenCompraMapper.toResponse(null)).isNull();
        assertThat(ordenCompraMapper.toResponseList(null)).isNull();
        assertThat(ordenCompraMapper.toDetalleResponse(null)).isNull();
        assertThat(ordenCompraMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Lista WHEN OrdenCompra To Response List THEN Mapea")
    void givenLista_whenOrdenCompraToResponseList_thenMapea() {
        assertThat(ordenCompraMapper.toResponseList(List.of(ordenCompraDemo()))).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN DetalleOrdenCompra WHEN To Detalle Response THEN Mapea")
    void givenDetalleOrdenCompra_whenToDetalleResponse_thenMapea() {
        DetalleOrdenCompra detalle = ordenCompraDemo().getDetalles().get(0);

        var dto = ordenCompraMapper.toDetalleResponse(detalle);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getCantidad()).isEqualTo(5);
        assertThat(dto.getProducto().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("GIVEN OrdenCompra Request WHEN To Entity THEN Resuelve Sucursal")
    void givenOrdenCompraRequest_whenToEntity_thenResuelveSucursal() {
        OrdenCompraRequestDTO request = new OrdenCompraRequestDTO();
        request.setProveedor("Distribuidora XYZ");
        request.setSucursal(new IdRefDTO(3L));

        OrdenCompra entity = ordenCompraMapper.toEntity(request);

        assertThat(entity.getProveedor()).isEqualTo("Distribuidora XYZ");
        assertThat(entity.getSucursal().getId()).isEqualTo(3L);
        assertThat(entity.getFecha()).isNull();
        assertThat(entity.getEstado()).isNull();
        assertThat(entity.getDetalles()).isNullOrEmpty();
    }

    @Test
    @DisplayName("GIVEN Ref Nula O Sin Id WHEN Id Ref To Sucursal THEN Retorna Null")
    void givenRefNulaOSinId_whenIdRefToSucursal_thenRetornaNull() {
        assertThat(ordenCompraMapper.idRefToSucursal(null)).isNull();
        assertThat(ordenCompraMapper.idRefToSucursal(new IdRefDTO())).isNull();
        assertThat(ordenCompraMapper.idRefToSucursal(new IdRefDTO(7L)).getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("GIVEN OrdenCompra Sin Detalles WHEN To Response THEN Mapea Sin Lista Nula")
    void givenOrdenCompraSinDetalles_whenToResponse_thenMapea() {
        OrdenCompra orden = new OrdenCompra();
        orden.setId(11L);
        orden.setProveedor("Solo proveedor");
        orden.setEstado("PENDIENTE");
        orden.setFecha(new Date());
        orden.setDetalles(null);

        var dto = ordenCompraMapper.toResponse(orden);

        assertThat(dto.getId()).isEqualTo(11L);
        assertThat(dto.getDetalles()).isNull();
        assertThat(dto.getSucursal()).isNull();
    }

    @Test
    @DisplayName("GIVEN Request Sin Sucursal WHEN To Entity THEN Sucursal Null")
    void givenRequestSinSucursal_whenToEntity_thenSucursalNull() {
        OrdenCompraRequestDTO request = new OrdenCompraRequestDTO();
        request.setProveedor("Sin sede");
        request.setSucursal(null);

        assertThat(ordenCompraMapper.toEntity(request).getSucursal()).isNull();
    }

    @Test
    @DisplayName("GIVEN Detalle Sin Producto WHEN To Detalle Response THEN Producto Null")
    void givenDetalleSinProducto_whenToDetalleResponse_thenProductoNull() {
        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setId(101L);
        detalle.setCantidad(1);
        detalle.setCostoUnitario(10.0);
        detalle.setProducto(null);

        assertThat(ordenCompraMapper.toDetalleResponse(detalle).getProducto()).isNull();
    }

    private OrdenCompra ordenCompraDemo() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Providencia");
        sucursal.setActiva(true);

        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Arroz");
        producto.setPrecio(1000.0);
        producto.setStock(10);
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");
        producto.setCategoria(categoria);

        OrdenCompra orden = new OrdenCompra();
        orden.setId(10L);
        orden.setProveedor("Proveedor Demo");
        orden.setSucursal(sucursal);
        orden.setFecha(new Date());
        orden.setEstado("PENDIENTE");

        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setId(100L);
        detalle.setOrdenCompra(orden);
        detalle.setProducto(producto);
        detalle.setCantidad(5);
        detalle.setCostoUnitario(500.0);
        orden.setDetalles(List.of(detalle));

        return orden;
    }
}
