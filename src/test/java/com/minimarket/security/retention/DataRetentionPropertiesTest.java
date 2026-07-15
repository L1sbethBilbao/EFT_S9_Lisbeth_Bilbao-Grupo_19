package com.minimarket.security.retention;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

class DataRetentionPropertiesTest {

    @Test
    @DisplayName("GIVEN Default Context WHEN Getters Y Setters THEN Funcionan Correctamente")
    void givenDefaultContext_whenGettersYSetters_thenFuncionanCorrectamente() {
        DataRetentionProperties properties = new DataRetentionProperties();

        properties.setEnabled(false);
        properties.setInactiveDays(30);
        properties.setCron("0 0 3 * * *");

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getInactiveDays()).isEqualTo(30);
        assertThat(properties.getCron()).isEqualTo("0 0 3 * * *");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Valores Por Defecto THEN Estan Configurados")
    void givenDefaultContext_whenValoresPorDefecto_thenEstanConfigurados() {
        DataRetentionProperties properties = new DataRetentionProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getInactiveDays()).isEqualTo(90);
        assertThat(properties.getCron()).isEqualTo("0 0 2 * * *");
    }
}
