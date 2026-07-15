# Resultados de validación — Semana 7 OpenAPI

Generado automáticamente por `OpenApiSemana7EvidenciaTest`.
Equivalente a probar los 10 endpoints en Swagger UI y Postman con los mismos tokens.

## Tabla de evidencias (Producto + Carrito)

| ID | Endpoint | Auth | HTTP | Análisis |
|----|----------|------|------|----------|
| E01 | GET /api/productos | Público | 200 | Catálogo público accesible sin autenticación |
| E02 | GET /api/productos/1 | Público | 200 | Detalle de producto existente retorna 200 |
| E03 | POST /api/productos | Bearer JWT | 200 | GERENTE crea producto id=10 |
| E04 | PUT /api/productos/1 | Bearer JWT | 200 | GERENTE actualiza producto; body validado por DTO |
| E05 | DELETE /api/productos/10 | Bearer JWT | 204 | GERENTE elimina producto creado en E03; respuesta 204 No Content |
| E06 | GET /api/carrito | Bearer JWT | 200 | CLIENTE autenticado lista su carrito |
| E07 | POST /api/carrito | Bearer JWT | 200 | Item creado con id=2; coincide con documentación OpenAPI |
| E08 | GET /api/carrito/2 | Bearer JWT | 200 | Item de carrito recuperado por ID |
| E09 | PUT /api/carrito/2 | Bearer JWT | 200 | Cantidad actualizada correctamente |
| E10 | DELETE /api/carrito/2 | Bearer JWT | 204 | DELETE /api/** exige GERENTE en filter chain; item eliminado con 204 |
## Swagger UI

- URL: http://localhost:8080/swagger-ui/index.html
- Tags: **Productos**, **Carrito**
- Repetir cada operación con los mismos bodies de ejemplo documentados en `@ExampleObject`

## Postman

- Importar: `postman/S8_01_OpenAPI_importar.json` o `postman/S8_02_Coleccion_endpoints_hateoas.json`
- Login: `POST /api/auth/login` → pegar `accessToken` en Authorization Bearer
- Consistencia: ver `postman/S7_CONSISTENCIA_swagger_postman.md`
