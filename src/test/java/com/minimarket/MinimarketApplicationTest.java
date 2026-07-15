package com.minimarket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class MinimarketApplicationTest {

    @Test
    @DisplayName("GIVEN Default Context WHEN Context Loads THEN Succeeds")
    void givenDefaultContext_whenContextLoads_thenSucceeds() {
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Main THEN Inicia Aplicacion Spring Boot")
    void givenDefaultContext_whenMain_thenIniciaAplicacionSpringBoot() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            MinimarketApplication.main(new String[]{});

            springApplication.verify(() -> SpringApplication.run(MinimarketApplication.class, new String[]{}));
        }
    }
}
