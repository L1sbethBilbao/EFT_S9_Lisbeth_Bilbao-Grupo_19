package com.minimarket.controller;

import com.minimarket.security.retention.DataRetentionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataRetentionControllerTest {

    @Mock
    private DataRetentionService dataRetentionService;

    @InjectMocks
    private DataRetentionController dataRetentionController;

    @Test
    @DisplayName("GIVEN Default Context WHEN Run Retention THEN Retorna Resumen")
    void givenDefaultContext_whenRunRetention_thenRetornaResumen() {
        when(dataRetentionService.anonymizeInactiveUsers()).thenReturn(3);

        ResponseEntity<Map<String, Object>> response = dataRetentionController.runRetention();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("message", "Proceso de retención ejecutado")
                .containsEntry("anonymizedCount", 3);
    }
}
