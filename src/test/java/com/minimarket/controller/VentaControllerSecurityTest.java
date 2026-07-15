package com.minimarket.controller;

import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VentaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private String ventaPayload;

    @BeforeEach
    void setUp() {
        Long empleadoId = usuarioRepository.findByUsername("empleado1").orElseThrow().getId();
        Long productoId = productoRepository.findAll().get(0).getId();
        ventaPayload = """
                {
                  "usuarioId": %d,
                  "items": [{ "productoId": %d, "cantidad": 1 }]
                }
                """.formatted(empleadoId, productoId);
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Empleado WHEN Registrar Venta THEN Retorna200")
    void givenEmpleado_whenRegistrarVenta_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/ventas/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaPayload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Gerente WHEN Registrar Venta THEN Retorna200")
    void givenGerente_whenRegistrarVenta_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/ventas/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaPayload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Cliente WHEN Registrar Venta THEN Retorna403")
    void givenCliente_whenRegistrarVenta_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/ventas/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaPayload))
                .andExpect(status().isForbidden());
    }
}
