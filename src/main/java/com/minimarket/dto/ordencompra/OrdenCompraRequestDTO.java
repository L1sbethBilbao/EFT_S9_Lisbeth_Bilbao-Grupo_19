package com.minimarket.dto.ordencompra;

import com.minimarket.dto.common.IdRefDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrdenCompraRequestDTO {

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    @NotNull(message = "La sucursal es obligatoria")
    private IdRefDTO sucursal;

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public IdRefDTO getSucursal() {
        return sucursal;
    }

    public void setSucursal(IdRefDTO sucursal) {
        this.sucursal = sucursal;
    }
}
