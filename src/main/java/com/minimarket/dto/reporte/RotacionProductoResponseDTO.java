package com.minimarket.dto.reporte;

public class RotacionProductoResponseDTO {

    private Long productoId;
    private String productoNombre;
    private Integer cantidadVentas;
    private Integer cantidadPedidos;
    private Integer totalRotacion;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Integer cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public Integer getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(Integer cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public Integer getTotalRotacion() {
        return totalRotacion;
    }

    public void setTotalRotacion(Integer totalRotacion) {
        this.totalRotacion = totalRotacion;
    }
}
