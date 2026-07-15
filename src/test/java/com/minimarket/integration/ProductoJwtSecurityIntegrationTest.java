package com.minimarket.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoJwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String productoPayload;

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

        productoPayload = """
                {
                  "nombre": "Producto JWT Test",
                  "precio": 990,
                  "stock": 5,
                  "descripcion": "Flujo JWT completo",
                  "categoria": { "id": 1 }
                }
                """;
    }

    @Test
    @DisplayName("GIVEN Empleado Con Jwt WHEN Crear Producto THEN Retorna403")
    void givenEmpleadoConJwt_whenCrearProducto_thenRetorna403() throws Exception {
        String token = loginAndGetAccessToken("empleado1", "empleado123");

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GIVEN Cliente Con Jwt WHEN Crear Producto THEN Retorna403")
    void givenClienteConJwt_whenCrearProducto_thenRetorna403() throws Exception {
        String token = loginAndGetAccessToken("cliente1", "cliente123");

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
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
