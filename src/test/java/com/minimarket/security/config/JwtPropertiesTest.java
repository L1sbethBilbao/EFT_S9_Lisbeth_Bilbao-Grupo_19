package com.minimarket.security.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtPropertiesTest {

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    @DisplayName("GIVEN Default Context WHEN Carga Valores Desde Application Properties THEN Succeeds")
    void givenDefaultContext_whenCargaValoresDesdeApplicationProperties_thenSucceeds() {
        assertThat(jwtProperties.getSecret()).isNotBlank();
        assertThat(jwtProperties.getAccessExpiration()).isEqualTo(900_000L);
        assertThat(jwtProperties.getRefreshExpiration()).isEqualTo(604_800_000L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Get Access Expiration THEN Usa Expiration Si Access Es Cero")
    void givenDefaultContext_whenGetAccessExpiration_thenUsaExpirationSiAccessEsCero() {
        JwtProperties props = new JwtProperties();
        props.setExpiration(120_000L);
        props.setAccessExpiration(0L);

        assertThat(props.getAccessExpiration()).isEqualTo(120_000L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Get Expiration THEN Devuelve Valor Configurado")
    void givenDefaultContext_whenGetExpiration_thenDevuelveValorConfigurado() {
        JwtProperties props = new JwtProperties();
        props.setExpiration(180_000L);

        assertThat(props.getExpiration()).isEqualTo(180_000L);
    }
}
