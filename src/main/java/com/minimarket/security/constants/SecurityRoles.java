package com.minimarket.security.constants;

public final class SecurityRoles {

    public static final String CLIENTE = "CLIENTE";
    public static final String EMPLEADO = "EMPLEADO";
    public static final String GERENTE = "GERENTE";

    public static final String ROLE_PREFIX = "ROLE_";

    private SecurityRoles() {
    }

    public static String toAuthority(String roleName) {
        if (roleName.startsWith(ROLE_PREFIX)) {
            return roleName;
        }
        return ROLE_PREFIX + roleName;
    }
}
