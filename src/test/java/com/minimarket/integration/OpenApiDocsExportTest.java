package com.minimarket.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsExportTest {

    private static final Path EXPORT_PATH = Path.of("postman", "S8_01_OpenAPI_importar.json");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GIVEN App Context WHEN Export OpenAPI THEN Genera JSON Valido Para Postman")
    void givenAppContext_whenExportOpenApi_thenGeneraJsonValidoParaPostman() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("MiniMarket Plus API"))
                .andExpect(jsonPath("$.info.version").value("1.1.0"))
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
                .andExpect(jsonPath("$.paths['/api/productos']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertThat(content).isNotEmpty();

        Files.createDirectories(EXPORT_PATH.getParent());
        Files.write(EXPORT_PATH, content);

        assertThat(Files.exists(EXPORT_PATH)).isTrue();
        assertThat(Files.size(EXPORT_PATH)).isGreaterThan(1000);
    }
}
