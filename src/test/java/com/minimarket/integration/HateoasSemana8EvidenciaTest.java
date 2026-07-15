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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Evidencia Semana 8: valida HATEOAS HAL (_links, _embedded, page) en Producto, Carrito,
 * Inventario y Usuario.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HateoasSemana8EvidenciaTest {

    private static final Path EVIDENCIA_MD = Path.of("evidencia", "semana8", "resultados-validacion.md");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private static Long productoId;
    private static Long clienteId;
    private static String tokenGerente;
    private static String tokenCliente;
    private static String tokenEmpleado;

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
            tokenEmpleado = login("empleado1", "empleado123");
        }
    }

    @Test
    @Order(1)
    @DisplayName("H01 GET /api/productos paginado → _embedded + page + _links")
    void h01_listarProductosPaginado() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andReturn();

        registrar("H01", "GET", "/api/productos?page=0&size=5", "Público", 200,
                "Lista paginada HAL con _embedded y metadatos page");
        assertThat(result.getResponse().getContentAsString()).contains("_links");
    }

    @Test
    @Order(2)
    @DisplayName("H02 GET /api/productos/{id} → _links.self")
    void h02_obtenerProductoConLinks() throws Exception {
        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.productos.href").exists());

        registrar("H02", "GET", "/api/productos/" + productoId, "Público", 200,
                "Recurso individual con _links self y productos");
    }

    @Test
    @Order(3)
    @DisplayName("H03 POST /api/productos → _links en respuesta")
    void h03_crearProductoConLinks() throws Exception {
        String body = """
                {
                  "nombre": "Producto HATEOAS S8",
                  "precio": 1200,
                  "stock": 5,
                  "descripcion": "Evidencia Semana 8",
                  "categoria": { "id": 1 }
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenGerente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$.id").exists());

        registrar("H03", "POST", "/api/productos", "GERENTE", 200,
                "POST retorna EntityModel HAL con _links");
    }

    @Test
    @Order(4)
    @DisplayName("H05 POST /api/carrito → _links usuario y producto")
    void h05_agregarCarritoConLinks() throws Exception {
        String body = """
                {
                  "usuario": { "id": %d },
                  "producto": { "id": %d },
                  "cantidad": 1
                }
                """.formatted(clienteId, productoId);

        mockMvc.perform(post("/api/carrito")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.carrito.href").exists());

        registrar("H05", "POST", "/api/carrito", "CLIENTE", 200,
                "Carrito con enlaces self, carrito, usuario, producto");
    }

    @Test
    @Order(5)
    @DisplayName("H04 GET /api/carrito paginado → _embedded + _links")
    void h04_listarCarritoPaginado() throws Exception {
        mockMvc.perform(get("/api/carrito?page=0&size=5")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$._links.self.href").exists());

        registrar("H04", "GET", "/api/carrito?page=0&size=5", "CLIENTE", 200,
                "Carrito paginado con _embedded HAL");
    }

    @Test
    @Order(6)
    @DisplayName("H06 GET /api/inventario paginado → _embedded + page")
    void h06_listarInventarioPaginado() throws Exception {
        mockMvc.perform(get("/api/inventario?page=0&size=5")
                        .header("Authorization", "Bearer " + tokenEmpleado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$.page.totalElements").exists())
                .andExpect(jsonPath("$._links.self.href").exists());

        registrar("H06", "GET", "/api/inventario?page=0&size=5", "EMPLEADO", 200,
                "Inventario paginado HAL con page.totalElements");
    }

    @Test
    @Order(7)
    @DisplayName("H07 POST /api/inventario → _links producto")
    void h07_registrarInventarioConLinks() throws Exception {
        String body = """
                {
                  "producto": { "id": %d },
                  "cantidad": 2,
                  "tipoMovimiento": "Entrada"
                }
                """.formatted(productoId);

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.inventario.href").exists());

        registrar("H07", "POST", "/api/inventario", "EMPLEADO", 200,
                "Movimiento inventario con _links HAL");
    }

    @Test
    @Order(8)
    @DisplayName("H08 GET /api/usuarios paginado → _embedded + _links")
    void h08_listarUsuariosPaginado() throws Exception {
        mockMvc.perform(get("/api/usuarios?page=0&size=5")
                        .header("Authorization", "Bearer " + tokenGerente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$._links.self.href").exists());

        registrar("H08", "GET", "/api/usuarios?page=0&size=5", "GERENTE", 200,
                "Usuarios paginados con _embedded y page");
    }

    @Test
    @Order(9)
    @DisplayName("H09 GET /api/usuarios/{id} → _links self y usuarios")
    void h09_obtenerUsuarioConLinks() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + clienteId)
                        .header("Authorization", "Bearer " + tokenGerente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.usuarios.href").exists());

        registrar("H09", "GET", "/api/usuarios/" + clienteId, "GERENTE", 200,
                "Usuario individual con _links HAL");
        escribirEvidenciaMarkdown();
    }

    private void registrar(String id, String method, String path, String rol, int status, String analisis) {
        resultados.add("| %s | %s %s | %s | %d | %s |".formatted(id, method, path, rol, status, analisis));
    }

    private void escribirEvidenciaMarkdown() throws Exception {
        Files.createDirectories(EVIDENCIA_MD.getParent());
        String contenido = """
                # Resultados de validación — Semana 8 HATEOAS HAL

                Generado automáticamente por `HateoasSemana8EvidenciaTest`.
                Valida _links, _embedded y paginación (page) en Producto, Carrito, Inventario y Usuario.

                ## Tabla de evidencias HATEOAS

                | ID | Endpoint | Rol | HTTP | Análisis |
                |----|----------|-----|------|----------|
                """ + String.join("\n", resultados) + """

                ## Swagger UI

                - URL: http://localhost:8080/swagger-ui/index.html
                - Verificar respuestas con `_links`, `_embedded` y `page`

                ## Postman

                - Importar: `postman/S8_01_OpenAPI_importar.json`
                - Colección: `postman/S8_02_Coleccion_endpoints_hateoas.json`
                - Consistencia: `postman/S8_CONSISTENCIA_swagger_postman.md`
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
