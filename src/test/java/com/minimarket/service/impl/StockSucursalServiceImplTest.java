package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
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
class StockSucursalServiceImplTest {

    @Mock
    private StockSucursalRepository stockSucursalRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private StockSucursalServiceImpl stockSucursalService;

    private Producto producto;
    private Sucursal sucursal;
    private StockSucursal stock;

    @BeforeEach
    void setUp() {
        producto = TestFixtures.productoArrozConStock(10);
        sucursal = TestFixtures.sucursalProvidencia();
        stock = TestFixtures.stockSucursal(producto, sucursal, 10, 5);
    }

    @Test
    @DisplayName("GIVEN Sucursal Valida WHEN Consultar Disponibilidad THEN Retorna Productos")
    void givenSucursalValida_whenConsultarDisponibilidad_thenRetornaProductos() {
        when(sucursalRepository.existsById(1L)).thenReturn(true);
        when(stockSucursalRepository.findBySucursalId(1L)).thenReturn(List.of(stock));

        assertThat(stockSucursalService.consultarDisponibilidad(1L))
                .hasSize(1)
                .first()
                .satisfies(dto -> {
                    assertThat(dto.getProductoId()).isEqualTo(10L);
                    assertThat(dto.getDisponible()).isTrue();
                });
    }

    @Test
    @DisplayName("GIVEN Stock Suficiente WHEN Validar Stock THEN No Lanza Excepcion")
    void givenStockSuficiente_whenValidarStock_thenNoLanzaExcepcion() {
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L))
                .thenReturn(Optional.of(stock));

        stockSucursalService.validarStockDisponible(1L, 10L, 3);
    }

    @Test
    @DisplayName("GIVEN Stock Insuficiente WHEN Validar Stock THEN Lanza Excepcion")
    void givenStockInsuficiente_whenValidarStock_thenLanzaExcepcion() {
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L))
                .thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> stockSucursalService.validarStockDisponible(1L, 10L, 20))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("GIVEN Stock Suficiente WHEN Decrementar Stock THEN Actualiza Cantidad")
    void givenStockSuficiente_whenDecrementarStock_thenActualizaCantidad() {
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L))
                .thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        stockSucursalService.decrementarStock(1L, 10L, 3);

        assertThat(stock.getCantidad()).isEqualTo(7);
        verify(stockSucursalRepository).save(stock);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Incrementar Stock THEN Suma Cantidad")
    void givenDefaultContext_whenIncrementarStock_thenSumaCantidad() {
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L))
                .thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        stockSucursalService.incrementarStock(1L, 10L, 5);

        assertThat(stock.getCantidad()).isEqualTo(15);
    }

    @Test
    @DisplayName("GIVEN Stock Insuficiente WHEN Decrementar Stock THEN No Persiste")
    void givenStockInsuficiente_whenDecrementarStock_thenNoPersiste() {
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L))
                .thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> stockSucursalService.decrementarStock(1L, 10L, 20))
                .isInstanceOf(StockInsuficienteException.class);

        verify(stockSucursalRepository, never()).save(any());
    }
}
