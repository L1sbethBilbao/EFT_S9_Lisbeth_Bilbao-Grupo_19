package com.minimarket.security.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

class AccountLockedExceptionTest {

    @Test
    @DisplayName("GIVEN Default Context WHEN Mensaje Por Defecto THEN Succeeds")
    void givenDefaultContext_whenMensajePorDefecto_thenSucceeds() {
        assertThat(new AccountLockedException().getMessage())
                .contains("bloqueada");
    }
}
