package com.minimarket.controller;

import com.minimarket.dto.reporte.RotacionProductoResponseDTO;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Reportes", description = "Reportes de gestión MiniMarket Plus")
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Operation(summary = "Rotación de productos",
            description = "Combina ventas y pedidos para identificar productos más y menos vendidos")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Reporte de rotación"))
    @GetMapping("/rotacion-productos")
    public List<RotacionProductoResponseDTO> obtenerRotacionProductos() {
        return reporteService.obtenerRotacionProductos();
    }
}
