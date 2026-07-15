package com.minimarket.security.constants;

public final class SecurityExpressions {

    public static final String AUTENTICADO =
            "hasAnyRole('CLIENTE','EMPLEADO','GERENTE')";

    public static final String EMPLEADO_O_GERENTE =
            "hasAnyRole('EMPLEADO','GERENTE')";

    public static final String SOLO_GERENTE = "hasRole('GERENTE')";

    private SecurityExpressions() {
    }
}
