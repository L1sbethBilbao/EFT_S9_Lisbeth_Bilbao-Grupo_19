package com.minimarket.controller;

import com.minimarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    private String movimientoPayload;

    @BeforeEach
    void setUp() {
        Long productoId = productoRepository.findAll().get(0).getId();
        movimientoPayload = """
                {
                  "producto": { "id": %d },
                  "cantidad": 2,
                  "tipoMovimiento": "Entrada",
                  "fechaMovimiento": "2024-06-15T10:00:00.000Z"
                }
                """.formatted(productoId);
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Empleado WHEN Registrar Movimiento THEN Retorna200")
    void givenEmpleado_whenRegistrarMovimiento_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoPayload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Gerente WHEN Listar Inventario THEN Retorna200")
    void givenGerente_whenListarInventario_thenRetorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Cliente WHEN Registrar Movimiento THEN Retorna403")
    void givenCliente_whenRegistrarMovimiento_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimientoPayload))
                .andExpect(status().isForbidden());
    }
}
