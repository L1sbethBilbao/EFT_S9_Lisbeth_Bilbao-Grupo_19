package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.dto.venta.VentaItemDTO;
import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.exception.UsuarioIncompletoException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = TestFixtures.usuarioEmpleadoCompleto();
        producto = TestFixtures.productoArrozConStock(5);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Calcular Total THEN Suma Precios Por Cantidad")
    void givenDefaultContext_whenCalcularTotal_thenSumaPreciosPorCantidad() {
        DetalleVenta d1 = new DetalleVenta();
        d1.setPrecio(1000.0);
        d1.setCantidad(2);
        DetalleVenta d2 = new DetalleVenta();
        d2.setPrecio(500.0);
        d2.setCantidad(3);

        assertThat(ventaService.calcularTotal(List.of(d1, d2))).isEqualTo(3500.0);
    }

    @Test
    @DisplayName("GIVEN Producto Con Stock Suficiente WHEN Validar Stock THEN No Lanza Excepcion")
    void givenProductoConStockSuficiente_whenValidarStock_thenNoLanzaExcepcion() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        ventaService.validarStockDisponible(10L, 3);
    }

    @Test
    @DisplayName("GIVEN Producto Sin Stock Suficiente WHEN Validar Stock THEN Lanza Excepcion")
    void givenProductoSinStockSuficiente_whenValidarStock_thenLanzaExcepcion() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        assertThatThrownBy(() -> ventaService.validarStockDisponible(10L, 10))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");
    }

    @Test
    @DisplayName("GIVEN Con Stock Suficiente WHEN Registrar Venta THEN Guarda Venta")
    void givenConStockSuficiente_whenRegistrarVenta_thenGuardaVenta() {
        VentaRegistroDTO dto = TestFixtures.ventaRegistro(2L, 10L, 2);

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(99L);
            return v;
        });

        Venta result = ventaService.registrarVenta(dto);

        assertThat(result.getTotal()).isEqualTo(2000.0);
        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(producto.getStock()).isEqualTo(3);
        verify(usuarioService).validarDatosCompletos(usuario);
        verify(usuarioService).validarPuedeRegistrarVenta(usuario);
        verify(productoRepository).save(producto);
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    @DisplayName("GIVEN Stock Insuficiente WHEN Registrar Venta THEN No Persiste Producto")
    void givenStockInsuficiente_whenRegistrarVenta_thenNoPersisteProducto() {
        VentaRegistroDTO dto = TestFixtures.ventaRegistro(2L, 10L, 10);

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> ventaService.registrarVenta(dto))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");

        verify(productoRepository, never()).save(any());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Usuario Incompleto WHEN Registrar Venta THEN Lanza Excepcion")
    void givenUsuarioIncompleto_whenRegistrarVenta_thenLanzaExcepcion() {
        VentaRegistroDTO dto = TestFixtures.ventaRegistro(2L, 10L, 1);

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        org.mockito.Mockito.doThrow(new UsuarioIncompletoException("incompleto"))
                .when(usuarioService).validarDatosCompletos(usuario);

        assertThatThrownBy(() -> ventaService.registrarVenta(dto))
                .isInstanceOf(UsuarioIncompletoException.class)
                .hasMessage("incompleto");
    }

    @Test
    @DisplayName("GIVEN Multiples Items WHEN Registrar Venta THEN Calcula Total Y Descuenta Stock")
    void givenMultiplesItems_whenRegistrarVenta_thenCalculaTotalYDescuentaStock() {
        Producto arroz = TestFixtures.productoArrozConStock(10);
        Producto leche = TestFixtures.productoConStock(11L, "Leche", 500.0, 8);
        VentaRegistroDTO dto = TestFixtures.ventaRegistroMultiItem(2L, List.of(
                TestFixtures.ventaItem(10L, 2),
                TestFixtures.ventaItem(11L, 3)
        ));

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(arroz));
        when(productoRepository.findById(11L)).thenReturn(Optional.of(leche));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(100L);
            return v;
        });

        Venta result = ventaService.registrarVenta(dto);

        assertThat(result.getTotal()).isEqualTo(3500.0);
        assertThat(result.getDetalles()).hasSize(2);
        assertThat(arroz.getStock()).isEqualTo(8);
        assertThat(leche.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("GIVEN Lista Vacia WHEN Calcular Total THEN Retorna Cero")
    void givenListaVacia_whenCalcularTotal_thenRetornaCero() {
        assertThat(ventaService.calcularTotal(List.of())).isEqualTo(0.0);
        assertThat(ventaService.calcularTotal(null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("GIVEN Con Total Nulo WHEN Save THEN Asigna Cero")
    void givenConTotalNulo_whenSave_thenAsignaCero() {
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta result = ventaService.save(venta);

        assertThat(result.getTotal()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Usuario Id THEN Retorna Ventas Del Usuario")
    void givenDefaultContext_whenFindByUsuarioId_thenRetornaVentasDelUsuario() {
        Venta v = new Venta();
        v.setId(1L);
        v.setUsuario(usuario);
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(v));

        assertThat(ventaService.findByUsuarioId(1L)).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Ventas")
    void givenDefaultContext_whenFindAll_thenRetornaVentas() {
        Venta venta = new Venta();
        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        assertThat(ventaService.findAll()).containsExactly(venta);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Venta")
    void givenExiste_whenFindById_thenRetornaVenta() {
        Venta venta = new Venta();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        assertThat(ventaService.findById(1L)).isSameAs(venta);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(ventaService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Producto No Existe WHEN Validar Stock THEN Lanza Excepcion")
    void givenProductoNoExiste_whenValidarStock_thenLanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.validarStockDisponible(99L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    @DisplayName("GIVEN Stock Null WHEN Validar Stock THEN Lanza Excepcion")
    void givenStockNull_whenValidarStock_thenLanzaExcepcion() {
        producto.setStock(null);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> ventaService.validarStockDisponible(10L, 1))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("GIVEN Usuario No Existe WHEN Registrar Venta THEN Lanza Excepcion")
    void givenUsuarioNoExiste_whenRegistrarVenta_thenLanzaExcepcion() {
        VentaRegistroDTO dto = new VentaRegistroDTO();
        dto.setUsuarioId(99L);
        dto.setItems(List.of(new VentaItemDTO()));

        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.registrarVenta(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("GIVEN Producto No Existe WHEN Registrar Venta THEN Lanza Excepcion")
    void givenProductoNoExiste_whenRegistrarVenta_thenLanzaExcepcion() {
        VentaRegistroDTO dto = TestFixtures.ventaRegistro(2L, 99L, 1);

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.registrarVenta(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    @DisplayName("GIVEN Con Total Explicito WHEN Save THEN No Sobrescribe")
    void givenConTotalExplicito_whenSave_thenNoSobrescribe() {
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setTotal(1500.0);
        when(ventaRepository.save(venta)).thenReturn(venta);

        assertThat(ventaService.save(venta).getTotal()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("GIVEN Producto Desaparece En Segunda Consulta WHEN Registrar Venta THEN Lanza Excepcion")
    void givenProductoDesapareceEnSegundaConsulta_whenRegistrarVenta_thenLanzaExcepcion() {
        VentaRegistroDTO dto = TestFixtures.ventaRegistro(2L, 10L, 1);

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L))
                .thenReturn(Optional.of(producto))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.registrarVenta(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    @DisplayName("GIVEN Usuario Sin Rol Valido WHEN Registrar Venta THEN Lanza Excepcion")
    void givenUsuarioSinRolValido_whenRegistrarVenta_thenLanzaExcepcion() {
        VentaRegistroDTO dto = TestFixtures.ventaRegistro(2L, 10L, 1);

        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        org.mockito.Mockito.doThrow(new UsuarioIncompletoException("rol inválido"))
                .when(usuarioService).validarPuedeRegistrarVenta(usuario);

        assertThatThrownBy(() -> ventaService.registrarVenta(dto))
                .isInstanceOf(UsuarioIncompletoException.class)
                .hasMessage("rol inválido");
    }
}
