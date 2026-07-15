package com.minimarket.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SuspiciousActivityPropertiesTest {

    @Autowired
    private SuspiciousActivityProperties suspiciousActivityProperties;

    @Test
    @DisplayName("GIVEN Default Context WHEN Carga Valores Desde Application Properties THEN Succeeds")
    void givenDefaultContext_whenCargaValoresDesdeApplicationProperties_thenSucceeds() {
        assertThat(suspiciousActivityProperties.getFailedLoginThreshold()).isEqualTo(5);
        assertThat(suspiciousActivityProperties.getRequestThreshold()).isEqualTo(200);
        assertThat(suspiciousActivityProperties.getWindowMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Get Window Ms THEN Convierte Minutos A Milisegundos")
    void givenDefaultContext_whenGetWindowMs_thenConvierteMinutosAMilisegundos() {
        SuspiciousActivityProperties props = new SuspiciousActivityProperties();
        props.setWindowMinutes(10);

        assertThat(props.getWindowMs()).isEqualTo(600_000L);
    }
}
