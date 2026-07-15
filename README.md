# MiniMarket Plus — Backend Semana 6

Backend REST de **MiniMarket Plus** con Spring Boot 3, autenticación JWT, control de acceso por roles (cliente / cajero / administrador), pruebas unitarias con JUnit 5 y Mockito, y cobertura JaCoCo.

## Requisitos

- Java 17+
- Maven 3.9+
- PowerShell (para script de pruebas secuenciales)

## Ejecución

```powershell
cd Exp2_S6_Lisbeth_bilbao_Grupo_19
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Usuarios demo

| Usuario    | Contraseña   | Rol (negocio)   | Authority Spring |
|------------|--------------|-----------------|------------------|
| cliente1   | cliente123   | Cliente         | ROLE_CLIENTE     |
| empleado1  | empleado123  | Cajero          | ROLE_EMPLEADO    |
| gerente1   | gerente123   | Administrador   | ROLE_GERENTE     |

## Permisos por rol (Semana 6)

| Operación | CLIENTE | EMPLEADO (cajero) | GERENTE (admin) |
|-----------|---------|-------------------|-----------------|
| Consultar catálogo (GET productos/categorías) | Sí (público) | Sí | Sí |
| Gestionar carrito (autenticado) | Sí | Sí | Sí |
| Registrar venta (`POST /api/ventas/registrar`) | No (403) | Sí | Sí |
| Inventario (`/api/inventario/**`) | No (403) | Sí | Sí |
| Crear/editar productos y categorías | No (403) | No (403) | Sí |
| Eliminar recursos (`DELETE /api/**`) | No | No | Sí |
| Gestión de usuarios | No | No | Sí |

## Estructura del proyecto

```
src/main/java/com/minimarket/
├── config/              # DataInitializer (roles, usuarios, catálogo demo)
├── controller/          # REST controllers con @PreAuthorize
├── dto/                 # DTOs request/response
├── entity/              # Entidades JPA
├── exception/           # GlobalExceptionHandler
├── mapper/              # MapStruct
├── security/            # JWT, MFA, rate limit, audit
└── util/                # InputSanitizer (Jsoup XSS)

src/test/java/com/minimarket/
├── service/impl/        # Pruebas unitarias de negocio
├── security/            # Pruebas de autenticación y autorización
├── integration/         # Pruebas integración (venta, concurrencia stock)
├── dto/                 # Validación de DTOs
├── entity/              # Relaciones entre entidades
└── support/             # TestFixtures centralizado
```

## Seguridad implementada

- **JWT stateless** (JJWT 0.12.6, HMAC-SHA256) + refresh tokens
- **RBAC**: `ConfigSpringSecurity` + `@PreAuthorize`
- **MFA TOTP** para gerente
- **Rate limiting** en `/api/auth/**` (Bucket4j)
- **Bloqueo por intentos fallidos** persistente en BD
- **XSS**: Jsoup en `InputSanitizer`
- **DTOs + @Valid**: validación de entrada

## Pruebas y cobertura

```powershell
# Ejecutar todas las pruebas
.\mvnw.cmd test

# Pruebas + reporte JaCoCo + verificación de umbrales (≥80% bundle, ≥90% servicios clave)
.\mvnw.cmd verify
```

Reporte HTML: `target/site/jacoco/index.html`

Postman Semana 8:
- Environment: `postman/S8_03_Environment_local.json`
- Colección HATEOAS: `postman/S8_02_Coleccion_endpoints_hateoas.json`
- OpenAPI: `postman/S8_01_OpenAPI_importar.json`
- Guía: `postman/S8_GUIA_importar_y_probar.md`

## Nota de producción

Cambiar `jwt.secret` por variable de entorno. No commitear secretos reales.
