# Resultados de validación — Semana 8 HATEOAS HAL

Generado automáticamente por `HateoasSemana8EvidenciaTest`.
Valida _links, _embedded y paginación (page) en Producto, Carrito, Inventario y Usuario.

## Tabla de evidencias HATEOAS

| ID | Endpoint | Rol | HTTP | Análisis |
|----|----------|-----|------|----------|
| H01 | GET /api/productos?page=0&size=5 | Público | 200 | Lista paginada HAL con _embedded y metadatos page |
| H02 | GET /api/productos/1 | Público | 200 | Recurso individual con _links self y productos |
| H03 | POST /api/productos | GERENTE | 200 | POST retorna EntityModel HAL con _links |
| H05 | POST /api/carrito | CLIENTE | 200 | Carrito con enlaces self, carrito, usuario, producto |
| H04 | GET /api/carrito?page=0&size=5 | CLIENTE | 200 | Carrito paginado con _embedded HAL |
| H06 | GET /api/inventario?page=0&size=5 | EMPLEADO | 200 | Inventario paginado HAL con page.totalElements |
| H07 | POST /api/inventario | EMPLEADO | 200 | Movimiento inventario con _links HAL |
| H08 | GET /api/usuarios?page=0&size=5 | GERENTE | 200 | Usuarios paginados con _embedded y page |
| H09 | GET /api/usuarios/1 | GERENTE | 200 | Usuario individual con _links HAL |
## Swagger UI

- URL: http://localhost:8080/swagger-ui/index.html
- Verificar respuestas con `_links`, `_embedded` y `page`

## Postman

- Importar: `postman/S8_01_OpenAPI_importar.json`
- Colección: `postman/S8_02_Coleccion_endpoints_hateoas.json`
- Consistencia: `postman/S8_CONSISTENCIA_swagger_postman.md`
