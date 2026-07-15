package com.minimarket.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Sanitize THEN Removes Script Tag")
    void givenDefaultContext_whenSanitize_thenRemovesScriptTag() {
        String dirty = "Bueno <script>alert('xss')</script> bueno";
        String clean = inputSanitizer.sanitize(dirty);

        assertThat(clean).doesNotContain("<script>", "alert");
        assertThat(clean).contains("Bueno", "bueno");
    }

    @Test
    @DisplayName("GIVEN Null WHEN Sanitize THEN Retorna Null")
    void givenNull_whenSanitize_thenRetornaNull() {
        assertThat(inputSanitizer.sanitize(null)).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Sanitize Strict THEN Removes All Html")
    void givenDefaultContext_whenSanitizeStrict_thenRemovesAllHtml() {
        String dirty = "<b>negrita</b> texto";
        String clean = inputSanitizer.sanitizeStrict(dirty);

        assertThat(clean).doesNotContain("<b>");
        assertThat(clean).contains("negrita", "texto");
    }

    @Test
    @DisplayName("GIVEN Null WHEN Sanitize Strict THEN Retorna Null")
    void givenNull_whenSanitizeStrict_thenRetornaNull() {
        assertThat(inputSanitizer.sanitizeStrict(null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<img src=x onerror=alert(1)>",
            "' OR '1'='1'--",
            "   ",
            "&lt;script&gt;hack&lt;/script&gt;"
    })
    @DisplayName("GIVEN Payload Malicioso WHEN Sanitize Strict THEN Elimina Contenido Peligroso")
    void givenPayloadMalicioso_whenSanitizeStrict_thenEliminaContenidoPeligroso(String payload) {
        String clean = inputSanitizer.sanitizeStrict(payload);

        assertThat(clean).doesNotContain("<script", "onerror");
    }

    @Test
    @DisplayName("GIVEN Javascript Url WHEN Sanitize THEN Elimina Script")
    void givenJavascriptUrl_whenSanitize_thenEliminaScript() {
        String clean = inputSanitizer.sanitize("<a href=\"javascript:alert(1)\">link</a>");
        assertThat(clean).doesNotContain("javascript:");
    }

    @Test
    @DisplayName("GIVEN String Vacio WHEN Sanitize Strict THEN Retorna Vacio")
    void givenStringVacio_whenSanitizeStrict_thenRetornaVacio() {
        assertThat(inputSanitizer.sanitizeStrict("")).isEmpty();
    }
}
