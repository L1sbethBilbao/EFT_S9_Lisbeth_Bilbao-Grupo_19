package com.minimarket.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VentaRegistroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void resetDemoUsers() {
        for (String username : new String[]{"cliente1", "empleado1", "gerente1"}) {
            usuarioRepository.findByUsername(username).ifPresent(u -> {
                u.setFailedLoginAttempts(0);
                u.setAccountLocked(false);
                u.setLockedUntil(null);
                usuarioRepository.save(u);
            });
        }
    }

    @Test
    @DisplayName("GIVEN Empleado Autenticado WHEN Registrar Venta THEN Retorna200 Y Descuenta Stock")
    void givenEmpleadoAutenticado_whenRegistrarVenta_thenRetorna200YDescuentaStock() throws Exception {
        Long empleadoId = usuarioRepository.findByUsername("empleado1").orElseThrow().getId();
        var producto = productoRepository.findAll().get(0);
        int stockInicial = producto.getStock();
        String token = loginAndGetAccessToken("empleado1", "empleado123");

        String payload = """
                {
                  "usuarioId": %d,
                  "items": [{ "productoId": %d, "cantidad": 1 }]
                }
                """.formatted(empleadoId, producto.getId());

        mockMvc.perform(post("/api/ventas/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        var productoActualizado = productoRepository.findById(producto.getId()).orElseThrow();
        assertThat(productoActualizado.getStock()).isEqualTo(stockInicial - 1);
    }

    @Test
    @DisplayName("GIVEN Cliente Autenticado WHEN Registrar Venta THEN Retorna403")
    void givenClienteAutenticado_whenRegistrarVenta_thenRetorna403() throws Exception {
        Long clienteId = usuarioRepository.findByUsername("cliente1").orElseThrow().getId();
        Long productoId = productoRepository.findAll().get(0).getId();
        String token = loginAndGetAccessToken("cliente1", "cliente123");

        String payload = """
                {
                  "usuarioId": %d,
                  "items": [{ "productoId": %d, "cantidad": 1 }]
                }
                """.formatted(clienteId, productoId);

        mockMvc.perform(post("/api/ventas/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetAccessToken(String username, String password) throws Exception {
        String loginPayload = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
