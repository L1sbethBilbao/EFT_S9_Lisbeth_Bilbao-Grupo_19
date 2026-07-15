package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
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
class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = TestFixtures.usuarioClienteCompleto();
        producto = TestFixtures.productoArrozConStock(5);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista")
    void givenDefaultContext_whenFindAll_thenRetornaLista() {
        Carrito carrito = new Carrito();
        when(carritoRepository.findAll()).thenReturn(List.of(carrito));

        assertThat(carritoService.findAll()).containsExactly(carrito);
    }

    @Test
    @DisplayName("GIVEN Existe WHEN Find By Id THEN Retorna Carrito")
    void givenExiste_whenFindById_thenRetornaCarrito() {
        Carrito carrito = new Carrito();
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        assertThat(carritoService.findById(1L)).isSameAs(carrito);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Null")
    void givenNoExiste_whenFindById_thenRetornaNull() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(carritoService.findById(99L)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save THEN Guarda Carrito")
    void givenDefaultContext_whenSave_thenGuardaCarrito() {
        Carrito carrito = new Carrito();
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        assertThat(carritoService.save(carrito)).isSameAs(carrito);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        carritoService.deleteById(3L);

        verify(carritoRepository).deleteById(3L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Usuario Id THEN Retorna Items")
    void givenDefaultContext_whenFindByUsuarioId_thenRetornaItems() {
        Carrito carrito = new Carrito();
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(List.of(carrito));

        assertThat(carritoService.findByUsuarioId(5L)).containsExactly(carrito);
    }

    @Test
    @DisplayName("GIVEN Stock Suficiente WHEN Agregar Producto THEN Guarda Carrito")
    void givenStockSuficiente_whenAgregarProducto_thenGuardaCarrito() {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito result = carritoService.agregarProducto(1L, 10L, 2);

        assertThat(result.getUsuario()).isEqualTo(usuario);
        assertThat(result.getProducto()).isEqualTo(producto);
        assertThat(result.getCantidad()).isEqualTo(2);
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    @DisplayName("GIVEN Stock Insuficiente WHEN Agregar Producto THEN No Persiste Carrito")
    void givenStockInsuficiente_whenAgregarProducto_thenNoPersisteCarrito() {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> carritoService.agregarProducto(1L, 10L, 10))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Usuario No Encontrado WHEN Agregar Producto THEN Lanza Excepcion")
    void givenUsuarioNoEncontrado_whenAgregarProducto_thenLanzaExcepcion() {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarProducto(99L, 10L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Producto No Encontrado WHEN Agregar Producto THEN Lanza Excepcion")
    void givenProductoNoEncontrado_whenAgregarProducto_thenLanzaExcepcion() {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarProducto(1L, 99L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado");

        verify(carritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("GIVEN Producto Con Stock Suficiente WHEN Validar Stock THEN No Lanza Excepcion")
    void givenProductoConStockSuficiente_whenValidarStock_thenNoLanzaExcepcion() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        carritoService.validarStockDisponible(10L, 3);
    }

    @Test
    @DisplayName("GIVEN Producto Sin Stock Suficiente WHEN Validar Stock THEN Lanza Excepcion")
    void givenProductoSinStockSuficiente_whenValidarStock_thenLanzaExcepcion() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> carritoService.validarStockDisponible(10L, 10))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Arroz");
    }

    @Test
    @DisplayName("GIVEN Usuario Existente WHEN Validar Usuario Carrito THEN Retorna Usuario")
    void givenUsuarioExistente_whenValidarUsuarioCarrito_thenRetornaUsuario() {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));

        assertThat(carritoService.validarUsuarioCarrito(1L)).isEqualTo(usuario);
    }

    @Test
    @DisplayName("GIVEN Multiples Productos WHEN Agregar Productos THEN Respeta Stock Individual")
    void givenMultiplesProductos_whenAgregarProductos_thenRespetaStockIndividual() {
        Producto arroz = TestFixtures.productoArrozConStock(5);
        Producto leche = TestFixtures.productoConStock(11L, "Leche", 500.0, 3);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(arroz));
        when(productoRepository.findById(11L)).thenReturn(Optional.of(leche));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito item1 = carritoService.agregarProducto(1L, 10L, 2);
        Carrito item2 = carritoService.agregarProducto(1L, 11L, 1);

        assertThat(item1.getCantidad()).isEqualTo(2);
        assertThat(item2.getCantidad()).isEqualTo(1);
        assertThat(item1.getProducto().getNombre()).isEqualTo("Arroz");
        assertThat(item2.getProducto().getNombre()).isEqualTo("Leche");
    }
}
