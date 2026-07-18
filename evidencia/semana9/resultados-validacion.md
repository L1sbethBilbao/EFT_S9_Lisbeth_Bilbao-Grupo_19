# Resultados de validación — EFT Semana 9

La carpeta `evidencia/` conserva **solo Semana 9** (EFT). Las carpetas históricas
de Semana 7 y 8 se eliminaron de este repositorio.

**Comandos para capturas:** ver [`COMANDOS_CAPTURAS.md`](./COMANDOS_CAPTURAS.md) (arranque, Swagger, Postman HAL, `mvn verify`, JaCoCo).

## Respuesta a la retroalimentación Semana 8 (Prof. Marcelo)

Mensaje clave de S8: **"¡Sólo falta HATEOAS!"**

| Observación S8 | Estado en EFT S9 |
|----------------|------------------|
| Informe decía HATEOAS pero controllers no devolvían EntityModel | **Corregido:** controllers principales retornan `EntityModel` / `PagedModel` |
| Centralizar links con assemblers | **Hecho:** 11 clases en `com.minimarket.hateoas` |
| Ubicar / publicar `openapi.json` | **Hecho:** `evidencia/semana9/openapi.json` |
| Unificar esquemas y ejemplos OpenAPI | **Reforzado:** `@ExampleObject` en Pedidos, Sucursales, Promociones, StockSucursal (+ Productos/Carrito/etc. previos) |
| Evidencia concreta de `_links` / `_embedded` / `page` | **Validado:** colección Postman S9 (29/29) |

## Contrato OpenAPI

- Archivo estático: [`openapi.json`](./openapi.json)
- Runtime: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Copia Postman del contrato: `postman/S9_01_OpenAPI_importar.json`

## Pruebas automatizadas

```powershell
.\mvnw.cmd verify
```

- **Estado:** BUILD SUCCESS (exit code 0)
- **Pruebas:** ~520 tests, 0 failures
- **JaCoCo:** bundle ≥ 80%, servicios clave ≥ 90%
- Reporte: `target/site/jacoco/index.html`

## Checklist de capturas mínimas para el PDF AVA (evaluación)

**Postman:** una sola corrida Collection Runner → evidencia **E-INT-02** (cubre módulos S9, integración y HATEOAS). No hace falta una captura por cada E-MS / E-HAT.

| ID | Obligatoria | Qué es |
|----|-------------|--------|
| E-ENV-01 | Recomendada | java / mvnw -version |
| E-INT-01 | Sí | Árbol de paquetes IDE |
| E-INT-02 | Sí | Runner 78 PASS + scrolls |
| E-POM-01 | Recomendada | pom.xml dependencies |
| E-SEG-01 | Sí | Login JWT (Body con token) |
| E-SEG-03 | Sí | 401/403 por rol |
| E-TEST-01 | Sí | `mvn verify` BUILD SUCCESS |
| E-TEST-COV-01 | Sí | JaCoCo HTML |
| E-DOC-OAS-01 | Sí | Swagger UI |
| E-DOC-OAS-02 | Sí | `openapi.json` en carpeta |
| E-CODE-02 | Sí | Assembler/Controller EntityModel |

Archivos Runner listos: `capturas/E-INT-02_runner_*.png`, `E-MS-04_runner_*.png`, `E-MS-05_runner_*.png` (pegar bajo E-INT-02).


### Guía no técnica — captura HAL (ejemplo H01)

1. Levantar la app: `.\mvnw.cmd spring-boot:run`
2. Abrir Postman e importar `postman/S9_02` + `S9_03`
3. Ejecutar **H01 GET productos**
4. En la pestaña Body, verificar que aparezcan `_embedded`, `_links` y `page`
5. Capturar pantalla y pegar en el informe como E-DOC-HAT-02

### Guía no técnica — video (30–60 s recomendados)

1. Mostrar Swagger o Postman
2. Ejecutar un GET de producto/sucursal
3. Señalar en voz alta `_links.self` y `_embedded` (si es listado)
4. Decir explícitamente: “En Semana 8 faltaba HATEOAS; en la EFT ya está implementado con EntityModel y assemblers”

## Recursos CON HATEOAS vs SIN HATEOAS (honestidad)

**Con HAL:** Producto, Categoría, Carrito, Inventario, Usuario, Venta, Sucursal, StockSucursal (CRUD), Promoción (CRUD), Pedido, OrdenCompra.

**Sin HAL (a propósito):** Auth, promociones `/activas`, disponibilidad por sucursal, reporte de rotación, DetalleVenta, AuditLog, DataRetention.

## Checklist corto antes de entregar (derivado de retro S8)

- [x] Informe §23: assemblers + `EntityModel`/`PagedModel` + recursos CON/SIN HAL
- [x] Evidencias E-DOC-HAT-01/02/03 definidas (producto/sucursal, listado paginado, pedido)
- [x] `evidencia/semana9/openapi.json` visible y citado en §22.5
- [x] Informe no afirma HATEOAS en auth, reportes, disponibilidad, etc.
- [ ] Capturas reales pegadas en el PDF AVA (E-DOC-OAS + E-DOC-HAT)
- [ ] Video 30–60 s mostrando `_links` (respuesta a “solo falta HATEOAS”)

## Colección Postman S9

- Environment: `postman/S9_03_Environment_local.json`
- Collection: `postman/S9_02_Coleccion_endpoints_EFT.json`
- Guía: `postman/S9_GUIA_importar_y_probar.md`
- Última verificación automatizada del flujo: **29/29 PASS**
