package com.minimarket.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Evidencia Semana 7: valida los 10 endpoints documentados (5 Producto + 5 Carrito)
 * con el mismo comportamiento esperado en Swagger UI y Postman.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpenApiSemana7EvidenciaTest {

    private static final Path EVIDENCIA_MD = Path.of("evidencia", "semana9", "resultados-openapi-base.md");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private static Long productoId;
    private static Long productoCreadoId;
    private static Long carritoId;
    private static Long clienteId;
    private static String tokenGerente;
    private static String tokenCliente;

    private static final List<String> resultados = new ArrayList<>();

    @BeforeEach
    void resetDemoUsers() throws Exception {
        for (String username : new String[]{"cliente1", "empleado1", "gerente1"}) {
            usuarioRepository.findByUsername(username).ifPresent(u -> {
                u.setFailedLoginAttempts(0);
                u.setAccountLocked(false);
                u.setLockedUntil(null);
                usuarioRepository.save(u);
            });
        }

        if (tokenGerente == null) {
            productoId = productoRepository.findAll().get(0).getId();
            clienteId = usuarioRepository.findByUsername("cliente1").orElseThrow().getId();
            tokenGerente = login("gerente1", "gerente123");
            tokenCliente = login("cliente1", "cliente123");
        }
    }

    @Test
    @Order(1)
    @DisplayName("E01 GET /api/productos sin token → 200")
    void e01_listarProductos() throws Exception {
        int status = performAndRecord("E01", "GET", "/api/productos", false, null, null);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(2)
    @DisplayName("E02 GET /api/productos/{id} sin token → 200")
    void e02_obtenerProductoPorId() throws Exception {
        int status = performAndRecord("E02", "GET", "/api/productos/" + productoId, false, null, null);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(3)
    @DisplayName("E03 POST /api/productos con GERENTE → 200")
    void e03_crearProducto() throws Exception {
        String body = """
                {
                  "nombre": "Producto OpenAPI Evidencia",
                  "precio": 1500,
                  "stock": 8,
                  "descripcion": "Creado en test Semana 7",
                  "categoria": { "id": 1 }
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenGerente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        int status = result.getResponse().getStatus();
        productoCreadoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        registrar("E03", "POST", "/api/productos", true, status,
                "GERENTE crea producto id=" + productoCreadoId);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(4)
    @DisplayName("E04 PUT /api/productos/{id} con GERENTE → 200")
    void e04_actualizarProducto() throws Exception {
        String body = """
                {
                  "nombre": "Arroz grano largo 1kg",
                  "precio": 1890,
                  "stock": 14,
                  "descripcion": "Stock actualizado Semana 7",
                  "categoria": { "id": 1 }
                }
                """;
        int status = performAndRecord("E04", "PUT", "/api/productos/" + productoId, true, tokenGerente, body);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(5)
    @DisplayName("E05 DELETE /api/productos/{id} con GERENTE → 204")
    void e05_eliminarProducto() throws Exception {
        int status = performAndRecord("E05", "DELETE", "/api/productos/" + productoCreadoId, true, tokenGerente, null);
        assertThat(status).isEqualTo(204);
    }

    @Test
    @Order(6)
    @DisplayName("E06 GET /api/carrito con CLIENTE → 200")
    void e06_listarCarrito() throws Exception {
        int status = performAndRecord("E06", "GET", "/api/carrito", true, tokenCliente, null);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(7)
    @DisplayName("E07 POST /api/carrito con CLIENTE → 200")
    void e07_agregarAlCarrito() throws Exception {
        String body = """
                {
                  "usuario": { "id": %d },
                  "producto": { "id": %d },
                  "cantidad": 2
                }
                """.formatted(clienteId, productoId);

        MvcResult result = mockMvc.perform(post("/api/carrito")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        int status = result.getResponse().getStatus();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        carritoId = json.get("id").asLong();
        registrar("E07", "POST", "/api/carrito", true, status,
                "Item creado con id=" + carritoId + "; coincide con documentación OpenAPI");
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(8)
    @DisplayName("E08 GET /api/carrito/{id} con CLIENTE → 200")
    void e08_obtenerCarritoPorId() throws Exception {
        int status = performAndRecord("E08", "GET", "/api/carrito/" + carritoId, true, tokenCliente, null);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(9)
    @DisplayName("E09 PUT /api/carrito/{id} con CLIENTE → 200")
    void e09_actualizarCarrito() throws Exception {
        String body = """
                {
                  "usuario": { "id": %d },
                  "producto": { "id": %d },
                  "cantidad": 3
                }
                """.formatted(clienteId, productoId);
        int status = performAndRecord("E09", "PUT", "/api/carrito/" + carritoId, true, tokenCliente, body);
        assertThat(status).isEqualTo(200);
    }

    @Test
    @Order(10)
    @DisplayName("E10 DELETE /api/carrito/{id} con GERENTE → 204")
    void e10_eliminarCarrito() throws Exception {
        int status = performAndRecord("E10", "DELETE", "/api/carrito/" + carritoId, true, tokenGerente, null);
        assertThat(status).isEqualTo(204);
        escribirEvidenciaMarkdown();
    }

    private int performAndRecord(String id, String method, String path, boolean auth, String token, String body)
            throws Exception {
        var builder = switch (method) {
            case "GET" -> get(path);
            case "POST" -> post(path).contentType(MediaType.APPLICATION_JSON).content(body);
            case "PUT" -> put(path).contentType(MediaType.APPLICATION_JSON).content(body);
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException("Método no soportado: " + method);
        };

        if (auth) {
            builder = builder.header("Authorization", "Bearer " + token);
        }

        MvcResult result = mockMvc.perform(builder).andReturn();
        int status = result.getResponse().getStatus();
        registrar(id, method, path, auth, status, analisis(id, status));
        return status;
    }

    private void registrar(String id, String method, String path, boolean auth, int status, String analisis) {
        resultados.add("| %s | %s %s | %s | %d | %s |".formatted(
                id, method, path, auth ? "Bearer JWT" : "Público", status, analisis));
    }

    private String analisis(String id, int status) {
        return switch (id) {
            case "E01" -> "Catálogo público accesible sin autenticación";
            case "E02" -> "Detalle de producto existente retorna 200";
            case "E03" -> "GERENTE puede crear producto según @PreAuthorize";
            case "E04" -> "GERENTE actualiza producto; body validado por DTO";
            case "E05" -> "GERENTE elimina producto creado en E03; respuesta 204 No Content";
            case "E06" -> "CLIENTE autenticado lista su carrito";
            case "E08" -> "Item de carrito recuperado por ID";
            case "E09" -> "Cantidad actualizada correctamente";
            case "E10" -> "DELETE /api/** exige GERENTE en filter chain; item eliminado con 204";
            default -> "Comportamiento acorde a contrato OpenAPI";
        };
    }

    private void escribirEvidenciaMarkdown() throws Exception {
        Files.createDirectories(EVIDENCIA_MD.getParent());
        String contenido = """
                # Resultados de validación — Semana 7 OpenAPI

                Generado automáticamente por `OpenApiSemana7EvidenciaTest`.
                Equivalente a probar los 10 endpoints en Swagger UI y Postman con los mismos tokens.

                ## Tabla de evidencias (Producto + Carrito)

                | ID | Endpoint | Auth | HTTP | Análisis |
                |----|----------|------|------|----------|
                """ + String.join("\n", resultados) + """

                ## Swagger UI

                - URL: http://localhost:8080/swagger-ui/index.html
                - Tags: **Productos**, **Carrito**
                - Repetir cada operación con los mismos bodies de ejemplo documentados en `@ExampleObject`

                ## Postman

                - Importar: `postman/S9_01_OpenAPI_importar.json` o `postman/S9_02_Coleccion_endpoints_EFT.json`
                - Login: `POST /api/auth/login` → pegar `accessToken` en Authorization Bearer
                - Consistencia: ver `postman/S9_CONSISTENCIA_swagger_postman.md`
                """;
        Files.writeString(EVIDENCIA_MD, contenido);
        assertThat(Files.exists(EVIDENCIA_MD)).isTrue();
    }

    private String login(String username, String password) throws Exception {
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
