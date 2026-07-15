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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    private String productoPayload;
    private Long productoId;

    @BeforeEach
    void setUp() {
        productoId = productoRepository.findAll().get(0).getId();
        productoPayload = """
                {
                  "nombre": "Producto Seguridad",
                  "precio": 1500,
                  "stock": 10,
                  "descripcion": "Test seguridad roles",
                  "categoria": { "id": 1 }
                }
                """;
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Gerente WHEN Crear Producto THEN Retorna200")
    void givenGerente_whenCrearProducto_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Gerente WHEN Actualizar Producto THEN Retorna200")
    void givenGerente_whenActualizarProducto_thenRetorna200() throws Exception {
        mockMvc.perform(put("/api/productos/" + productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Empleado WHEN Crear Producto THEN Retorna403")
    void givenEmpleado_whenCrearProducto_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Empleado WHEN Actualizar Producto THEN Retorna403")
    void givenEmpleado_whenActualizarProducto_thenRetorna403() throws Exception {
        mockMvc.perform(put("/api/productos/" + productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Cliente WHEN Crear Producto THEN Retorna403")
    void givenCliente_whenCrearProducto_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Cliente WHEN Actualizar Producto THEN Retorna403")
    void givenCliente_whenActualizarProducto_thenRetorna403() throws Exception {
        mockMvc.perform(put("/api/productos/" + productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Empleado WHEN Eliminar Producto THEN Retorna403")
    void givenEmpleado_whenEliminarProducto_thenRetorna403() throws Exception {
        mockMvc.perform(delete("/api/productos/" + productoId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Cliente WHEN Eliminar Producto THEN Retorna403")
    void givenCliente_whenEliminarProducto_thenRetorna403() throws Exception {
        mockMvc.perform(delete("/api/productos/" + productoId))
                .andExpect(status().isForbidden());
    }
}
