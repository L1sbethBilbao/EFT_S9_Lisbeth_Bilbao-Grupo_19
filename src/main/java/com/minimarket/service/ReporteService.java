package com.minimarket.service;

import com.minimarket.dto.reporte.RotacionProductoResponseDTO;

import java.util.List;

public interface ReporteService {

    List<RotacionProductoResponseDTO> obtenerRotacionProductos();
}
