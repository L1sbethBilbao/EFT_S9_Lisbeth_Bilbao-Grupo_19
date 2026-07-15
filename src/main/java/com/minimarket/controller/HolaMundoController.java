package com.minimarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Público", description = "Endpoints públicos de verificación")
@RestController
public class HolaMundoController {

    @Operation(summary = "Saludo público", description = "Endpoint de prueba sin autenticación")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Mensaje de bienvenida"))
    @SecurityRequirements
    @GetMapping("/public/hola")
    public String holaMundo() {
        return "¡Hola Mundo!";
    }
}
