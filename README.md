# MiniMarket Plus — Backend EFT Semana 9

Backend REST de **MiniMarket Plus** con Spring Boot 3, gestión multi-sucursal, pedidos en línea, promociones, órdenes de compra automáticas, reportes de rotación, autenticación JWT, RBAC, OpenAPI, HATEOAS y pruebas unitarias con JaCoCo.

## Requisitos

- Java 17+
- Maven 3.9+

## Ejecución

```powershell
cd EFT_S9_Lisbeth_Bilbao-Grupo_19
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Usuarios demo

| Usuario    | Contraseña   | Rol (negocio)   | Authority Spring |
|------------|--------------|-----------------|------------------|
| cliente1   | cliente123   | Cliente         | ROLE_CLIENTE     |
| empleado1  | empleado123  | Cajero          | ROLE_EMPLEADO    |
| gerente1   | gerente123   | Administrador   | ROLE_GERENTE     |

## Nuevos módulos Semana 9 (EFT)

| Módulo | Endpoint base | Descripción |
|--------|---------------|-------------|
| Sucursales | `/api/sucursales` | CRUD de sucursales (GET público, mutaciones GERENTE) |
| Stock sucursal | `/api/stock-sucursal` | Stock por sucursal y disponibilidad pública |
| Promociones | `/api/promociones` | Ofertas centralizadas (GET público, CRUD GERENTE) |
| Pedidos | `/api/pedidos` | Pedidos RETIRO/DESPACHO con descuento y stock por sucursal |
| Órdenes de compra | `/api/ordenes-compra` | Generación automática y confirmación de recepción |
| Reportes | `/api/reportes` | Rotación de productos (ventas + pedidos) |

## Permisos destacados

| Operación | CLIENTE | EMPLEADO | GERENTE | Público |
|-----------|---------|----------|---------|---------|
| GET sucursales, promociones, disponibilidad | — | — | — | Sí |
| Registrar pedido | Sí | Sí | Sí | No |
| Actualizar estado pedido | No | Sí | Sí | No |
| Stock sucursal (mutaciones) | No | Sí | Sí | No |
| Órdenes de compra y reportes | No | Sí | Sí | No |
| CRUD sucursales/promociones | No | No | Sí | — |

## Datos iniciales (DataInitializer)

- 3 sucursales en Santiago (Providencia, Ñuñoa, Maipú)
- Stock por producto repartido entre sucursales
- 2 promociones activas
- Semilla de sucursales solo si `sucursalRepository.count() == 0`

## Pruebas y cobertura

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Reporte JaCoCo: `target/site/jacoco/index.html`

## Documentación API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

## Postman (EFT Semana 9)

| Archivo | Uso |
|---------|-----|
| `postman/S9_03_Environment_local.json` | Environment local |
| `postman/S9_02_Coleccion_endpoints_EFT.json` | Colección HATEOAS + negocio |
| `postman/S9_GUIA_importar_y_probar.md` | Orden exacto de pruebas |

Ver también `postman/README.md`.

## Evidencia

Ver `evidencia/semana9/resultados-validacion.md` para resultados de validación EFT.

**Comandos paso a paso para capturas** (Swagger, Postman HAL, tests, JaCoCo):  
`evidencia/semana9/COMANDOS_CAPTURAS.md`
