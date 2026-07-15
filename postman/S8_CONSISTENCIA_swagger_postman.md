# Consistencia Swagger UI vs Postman — Semana 8 (HATEOAS HAL)

Validación de endpoints con respuestas HAL (`_links`, `_embedded`, `page`) en Producto, Carrito, Inventario y Usuario.

## Archivos Semana 8

| Archivo | Uso |
|---------|-----|
| `S8_01_OpenAPI_importar.json` | OpenAPI 3 exportado (versión API 1.1.0) |
| `S8_02_Coleccion_endpoints_hateoas.json` | Colección H01–H09 con tests HAL |
| `S8_03_Environment_local.json` | Environment local (baseUrl, usuarios demo) |
| `evidencia/semana8/resultados-validacion.md` | Resultados automáticos H01–H09 |

## Pre-requisitos Postman

1. Levantar app: `.\mvnw.cmd spring-boot:run`
2. Importar environment: **`S8_03_Environment_local.json`**
3. Importar colección: **`S8_02_Coleccion_endpoints_hateoas.json`**
4. Ejecutar **S00**, **S00b**, **S00c** (tokens GERENTE, CLIENTE, EMPLEADO)
5. Ejecutar **H05 antes de H04** (carrito vacío no incluye `_embedded` en HAL)

## Tabla de consistencia HATEOAS

| ID | Método | Path | Rol | HTTP | HAL esperado | Validado auto |
|----|--------|------|-----|------|--------------|---------------|
| H01 | GET | `/api/productos?page=0&size=5` | Público | 200 | `_embedded`, `page`, `_links.self` | Sí |
| H02 | GET | `/api/productos/{id}` | Público | 200 | `_links.self`, `_links.productos` | Sí |
| H03 | POST | `/api/productos` | GERENTE | 200 | `_links.self`, `id` | Sí |
| H05 | POST | `/api/carrito` | CLIENTE | 200 | `_links.self`, `_links.carrito` | Sí |
| H04 | GET | `/api/carrito?page=0&size=5` | CLIENTE | 200 | `_embedded`, `page`, `_links.self` | Sí |
| H06 | GET | `/api/inventario?page=0&size=5` | EMPLEADO | 200 | `_embedded`, `page.totalElements` | Sí |
| H07 | POST | `/api/inventario` | EMPLEADO | 200 | `_links.self`, `_links.inventario` | Sí |
| H08 | GET | `/api/usuarios?page=0&size=5` | GERENTE | 200 | `_embedded`, `page.size=5` | Sí |
| H09 | GET | `/api/usuarios/{id}` | GERENTE | 200 | `_links.self`, `_links.usuarios` | Sí |

## Cambios respecto a Semana 7

| Aspecto | Semana 7 | Semana 8 |
|---------|----------|----------|
| GET listas | JSON array plano | `PagedModel` HAL con `_embedded` y `page` |
| GET/POST item | DTO plano | `EntityModel` con `_links` |
| Paginación | No | Query params `page` y `size` |
| OpenAPI info.version | 1.0.0 | 1.1.0 |

## Regenerar evidencia automática

```powershell
cd Exp3_S8_Lisbeth_Bilbao_Grupo19
.\mvnw.cmd test "-Dtest=HateoasSemana8EvidenciaTest,OpenApiDocsExportTest"
```

Actualiza `evidencia/semana8/resultados-validacion.md` y `postman/S8_01_OpenAPI_importar.json`.
