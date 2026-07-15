package com.minimarket.service.impl;

import com.minimarket.dto.venta.VentaRegistroDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaMultiItemParameterizedTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = TestFixtures.usuarioEmpleadoCompleto();
        when(usuarioService.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta venta = inv.getArgument(0);
            venta.setId(100L);
            return venta;
        });
    }

    @ParameterizedTest(name = "cantidadItems={0}")
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("GIVEN Cantidad De Items WHEN Registrar Venta THEN Total Coincide Con Items")
    void givenCantidadDeItems_whenRegistrarVenta_thenTotalCoincideConItems(int cantidadItems) {
        List<com.minimarket.dto.venta.VentaItemDTO> items = new ArrayList<>();
        double totalEsperado = 0.0;

        for (int i = 0; i < cantidadItems; i++) {
            long productoId = 10L + i;
            int cantidad = i + 1;
            double precio = 1000.0 * (i + 1);
            Producto producto = TestFixtures.productoConStock(productoId, "Producto" + i, precio, 20);
            when(productoRepository.findById(productoId)).thenReturn(Optional.of(producto));
            items.add(TestFixtures.ventaItem(productoId, cantidad));
            totalEsperado += precio * cantidad;
        }

        VentaRegistroDTO dto = TestFixtures.ventaRegistroMultiItem(2L, items);
        Venta venta = ventaService.registrarVenta(dto);

        assertThat(venta.getDetalles()).hasSize(cantidadItems);
        assertThat(venta.getTotal()).isEqualTo(totalEsperado);
    }
}
