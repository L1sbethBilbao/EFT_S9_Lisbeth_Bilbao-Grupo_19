package com.minimarket.security.config;

import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigSpringSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private String productoPayload;
    private String ventaPayload;
    private String inventarioPayload;
    private Long productoId;

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

        Long empleadoId = usuarioRepository.findByUsername("empleado1").orElseThrow().getId();
        productoId = productoRepository.findAll().get(0).getId();

        productoPayload = """
                {
                  "nombre": "Producto Test",
                  "precio": 1000,
                  "stock": 5,
                  "descripcion": "Descripcion test",
                  "categoria": { "id": 1 }
                }
                """;

        ventaPayload = """
                {
                  "usuarioId": %d,
                  "items": [{ "productoId": %d, "cantidad": 1 }]
                }
                """.formatted(empleadoId, productoId);

        inventarioPayload = """
                {
                  "producto": { "id": %d },
                  "cantidad": 3,
                  "tipoMovimiento": "Entrada",
                  "fechaMovimiento": "2024-06-15T10:00:00.000Z"
                }
                """.formatted(productoId);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Public Endpoint THEN Incluye Headers Owasp")
    void givenDefaultContext_whenPublicEndpoint_thenIncluyeHeadersOwasp() throws Exception {
        mockMvc.perform(get("/public/hola"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'self'; script-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Productos Sin Token THEN Retorna200")
    void givenDefaultContext_whenProductosSinToken_thenRetorna200() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Default Context WHEN Inventario Con Cliente THEN Retorna403")
    void givenDefaultContext_whenInventarioConCliente_thenRetorna403() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Default Context WHEN Usuarios Con Gerente THEN Retorna200")
    void givenDefaultContext_whenUsuariosConGerente_thenRetorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Login Credenciales Invalidas THEN Retorna401")
    void givenDefaultContext_whenLoginCredencialesInvalidas_thenRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cliente1\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Login Sql Injection THEN Retorna401")
    void givenDefaultContext_whenLoginSqlInjection_thenRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"' OR '1'='1'--\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Default Context WHEN Inventario Con Empleado THEN Retorna200")
    void givenDefaultContext_whenInventarioConEmpleado_thenRetorna200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Default Context WHEN Crear Producto Con Cliente THEN Retorna403")
    void givenDefaultContext_whenCrearProductoConCliente_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Default Context WHEN Crear Producto Con Empleado THEN Retorna403")
    void givenDefaultContext_whenCrearProductoConEmpleado_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Default Context WHEN Crear Producto Con Gerente THEN Retorna200")
    void givenDefaultContext_whenCrearProductoConGerente_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Default Context WHEN Registrar Venta Con Cliente THEN Retorna403")
    void givenDefaultContext_whenRegistrarVentaConCliente_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/ventas/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    @DisplayName("GIVEN Default Context WHEN Registrar Venta Con Empleado THEN Retorna200")
    void givenDefaultContext_whenRegistrarVentaConEmpleado_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/ventas/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaPayload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Login Credenciales Validas THEN Retorna200")
    void givenDefaultContext_whenLoginCredencialesValidas_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cliente1\",\"password\":\"cliente123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Default Context WHEN Crear Producto Con Xss THEN Sanitiza Descripcion")
    void givenDefaultContext_whenCrearProductoConXss_thenSanitizaDescripcion() throws Exception {
        String payload = """
                {
                  "nombre": "Producto XSS",
                  "precio": 1000,
                  "stock": 5,
                  "descripcion": "Texto <script>alert('xss')</script> seguro",
                  "categoria": { "id": 1 }
                }
                """;
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Texto seguro"));
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
    @DisplayName("GIVEN Empleado WHEN Actualizar Producto THEN Retorna403")
    void givenEmpleado_whenActualizarProducto_thenRetorna403() throws Exception {
        mockMvc.perform(put("/api/productos/" + productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    @DisplayName("GIVEN Cliente WHEN Eliminar Producto THEN Retorna403")
    void givenCliente_whenEliminarProducto_thenRetorna403() throws Exception {
        mockMvc.perform(delete("/api/productos/" + productoId))
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
    @DisplayName("GIVEN Cliente WHEN Registrar Movimiento Inventario THEN Retorna403")
    void givenCliente_whenRegistrarMovimientoInventario_thenRetorna403() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventarioPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    @DisplayName("GIVEN Gerente WHEN Registrar Movimiento Inventario THEN Retorna200")
    void givenGerente_whenRegistrarMovimientoInventario_thenRetorna200() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventarioPayload))
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
    @DisplayName("GIVEN Credenciales Nulas WHEN Login THEN Retorna400")
    void givenCredencialesNulas_whenLogin_thenRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":null,\"password\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GIVEN Credenciales Vacias WHEN Login THEN Retorna400")
    void givenCredencialesVacias_whenLogin_thenRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GIVEN Cuenta Bloqueada WHEN Login THEN Retorna429")
    void givenCuentaBloqueada_whenLogin_thenRetorna429() throws Exception {
        usuarioRepository.findByUsername("cliente1").ifPresent(u -> {
            u.setAccountLocked(true);
            u.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(u);
        });

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"cliente1\",\"password\":\"cliente123\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("GIVEN Sin Token WHEN Swagger UI THEN Retorna200")
    void givenSinToken_whenSwaggerUi_thenRetorna200() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GIVEN Sin Token WHEN OpenAPI Docs THEN Retorna200")
    void givenSinToken_whenOpenApiDocs_thenRetorna200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").exists());
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Registro Con Username Malicioso THEN Sanitiza Y Retorna200")
    void givenDefaultContext_whenRegistroConUsernameMalicioso_thenSanitizaYRetorna200() throws Exception {
        String uniqueUser = "usr" + System.nanoTime();
        String payload = """
                {
                  "username": "<script>hack</script>%s",
                  "password": "cliente123",
                  "nombre": "Test",
                  "apellido": "Usuario",
                  "email": "%s@test.cl",
                  "direccion": "Calle 1"
                }
                """.formatted(uniqueUser, uniqueUser);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(uniqueUser));
    }
}
