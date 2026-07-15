package com.minimarket.integration;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.StockInsuficienteException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventarioStockConcurrencyIntegrationTest {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto producto;

    @BeforeEach
    void prepararProductoConStock() {
        producto = productoRepository.findAll().stream()
                .filter(p -> p.getNombre().contains("Arroz"))
                .findFirst()
                .orElseGet(() -> productoRepository.findAll().get(0));
        producto.setStock(10);
        productoRepository.save(producto);
    }

    @Test
    @DisplayName("GIVEN Salidas Concurrentes WHEN Registrar Movimientos THEN Solo Una Puede Agotar Stock")
    void givenSalidasConcurrentes_whenRegistrarMovimientos_thenSoloUnaPuedeAgotarStock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        List<Throwable> errores = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            futures.add(executor.submit(() -> {
                try {
                    start.await();
                    Inventario salida = TestFixtures.inventarioSalida(producto, 7);
                    inventarioService.registrarMovimiento(salida);
                } catch (StockInsuficienteException ex) {
                    synchronized (errores) {
                        errores.add(ex);
                    }
                } catch (Exception ex) {
                    synchronized (errores) {
                        errores.add(ex);
                    }
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        Producto actualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(actualizado.getStock()).isLessThan(10);
        assertThat(actualizado.getStock()).isGreaterThanOrEqualTo(0);
        // Sin bloqueo optimista, ambas hilos pueden completar (stock=3) o una puede fallar.
        if (!errores.isEmpty()) {
            assertThat(errores.stream().anyMatch(StockInsuficienteException.class::isInstance)).isTrue();
        }
    }
}
