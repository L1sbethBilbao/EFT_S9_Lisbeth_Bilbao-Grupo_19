package com.minimarket.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RateLimitPropertiesTest {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Test
    @DisplayName("GIVEN Default Context WHEN Carga Valores Desde Application Properties THEN Succeeds")
    void givenDefaultContext_whenCargaValoresDesdeApplicationProperties_thenSucceeds() {
        assertThat(rateLimitProperties.getAuthPerMinute()).isEqualTo(30);
    }
}
