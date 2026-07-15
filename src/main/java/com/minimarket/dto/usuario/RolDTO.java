package com.minimarket.dto.usuario;

public class RolDTO {

    private Long id;
    private String nombre;

    public RolDTO() {
    }

    public RolDTO(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
