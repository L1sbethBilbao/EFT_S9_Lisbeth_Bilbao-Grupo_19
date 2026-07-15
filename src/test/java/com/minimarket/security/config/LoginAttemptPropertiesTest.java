package com.minimarket.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoginAttemptPropertiesTest {

    @Autowired
    private LoginAttemptProperties loginAttemptProperties;

    @Test
    @DisplayName("GIVEN Default Context WHEN Carga Valores Desde Application Properties THEN Succeeds")
    void givenDefaultContext_whenCargaValoresDesdeApplicationProperties_thenSucceeds() {
        assertThat(loginAttemptProperties.getMaxAttempts()).isEqualTo(5);
        assertThat(loginAttemptProperties.getLockMinutes()).isEqualTo(15);
    }
}
