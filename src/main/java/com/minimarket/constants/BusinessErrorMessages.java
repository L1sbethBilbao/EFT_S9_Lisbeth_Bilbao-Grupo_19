package com.minimarket.constants;

public final class BusinessErrorMessages {

    public static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado: %s";
    public static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado: %s";
    public static final String ROL_NO_ENCONTRADO = "Rol no encontrado: %s";
    public static final String USUARIO_DATOS_INCOMPLETOS =
            "El usuario debe tener nombre, apellido, email y dirección completos";
    public static final String USUARIO_SIN_ROL_VALIDO =
            "El usuario no tiene un rol válido para registrar ventas (se requiere cajero o administrador)";
    public static final String STOCK_INSUFICIENTE =
            "Stock insuficiente para producto %s: disponible=%s, solicitado=%s";
    public static final String TIPO_MOVIMIENTO_OBLIGATORIO =
            "El tipo de movimiento es obligatorio";
    public static final String CANTIDAD_OBLIGATORIA =
            "La cantidad del movimiento debe ser mayor a cero";
    public static final String TIPO_MOVIMIENTO_INVALIDO =
            "Tipo de movimiento no válido: %s. Use Entrada o Salida";

    private BusinessErrorMessages() {
    }
}
