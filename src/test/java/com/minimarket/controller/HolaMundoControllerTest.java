package com.minimarket.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HolaMundoControllerTest {

    @InjectMocks
    private HolaMundoController holaMundoController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Hola Mundo THEN Retorna Saludo")
    void givenDefaultContext_whenHolaMundo_thenRetornaSaludo() {
        assertThat(holaMundoController.holaMundo()).isEqualTo("¡Hola Mundo!");
    }
}
