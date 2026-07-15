package com.minimarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniMarket Plus API")
                        .description("API REST del backend MiniMarket Plus: productos, inventario, ventas, "
                                + "carrito y usuarios con roles CLIENTE, EMPLEADO y GERENTE. "
                                + "Autenticación JWT vía POST /api/auth/login. "
                                + "Respuestas HAL con HATEOAS (_links, _embedded) y paginación en listados.")
                        .version("1.1.0")
                        .contact(new Contact()
                                .name("Grupo 19")
                                .email("minimarket@example.com")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en POST /api/auth/login o /api/auth/register")));
    }
}
