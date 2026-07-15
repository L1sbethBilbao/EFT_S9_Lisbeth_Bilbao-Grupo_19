package com.minimarket.dto.carrito;

import com.minimarket.dto.common.IdRefDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;

public class CarritoResponseDTO {

    private Long id;
    private UsuarioResponseDTO usuario;
    private ProductoResponseDTO producto;
    private Integer cantidad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }

    public ProductoResponseDTO getProducto() {
        return producto;
    }

    public void setProducto(ProductoResponseDTO producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
