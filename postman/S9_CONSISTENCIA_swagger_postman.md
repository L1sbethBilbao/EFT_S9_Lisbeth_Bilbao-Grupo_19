# Consistencia Swagger ↔ Postman — EFT Semana 9

Tabla de verificación: endpoint en código/Swagger vs request en colección `S9_02`.

| Módulo | Endpoint | Método | Auth | Request Postman |
|--------|----------|--------|------|-----------------|
| Auth | `/api/auth/login` | POST | Público | S00 / S00b / S00c |
| Productos | `/api/productos` | GET | Público | H01 |
| Productos | `/api/productos/{id}` | GET | Público | H02 |
| Productos | `/api/productos` | POST | GERENTE | H03 |
| Carrito | `/api/carrito` | POST | CLIENTE | H05 |
| Carrito | `/api/carrito` | GET | CLIENTE | H04 |
| Inventario | `/api/inventario` | GET | EMPLEADO | H06 |
| Inventario | `/api/inventario` | POST | EMPLEADO | H07 |
| Usuarios | `/api/usuarios` | GET | GERENTE | H08 |
| Usuarios | `/api/usuarios/{id}` | GET | GERENTE | H09 |
| Categorías | `/api/categorias` | GET | Público | H10 |
| Ventas | `/api/ventas` | GET | AUTENTICADO | H11 |
| Sucursales | `/api/sucursales` | GET | Público | H12 |
| Sucursales | `/api/sucursales/{id}` | GET | Público | H13 |
| Promociones | `/api/promociones` | GET | Público | H14 |
| Promociones | `/api/promociones/activas` | GET | Público | N02 |
| Promociones | `/api/promociones` | POST | GERENTE | N11 |
| Stock | `/api/stock-sucursal` | GET | EMPLEADO | H15 |
| Stock | `/api/stock-sucursal/disponibilidad/{id}` | GET | Público | N01 |
| Pedidos | `/api/pedidos/registrar` | POST | AUTENTICADO | N03 / N04 |
| Pedidos | `/api/pedidos` | GET | AUTENTICADO | N05 |
| Pedidos | `/api/pedidos/{id}/estado` | PATCH | EMPLEADO | N06 |
| Órdenes | `/api/ordenes-compra/generar-automaticas` | POST | EMPLEADO | N07 |
| Órdenes | `/api/ordenes-compra` | GET | EMPLEADO | N08 |
| Órdenes | `/api/ordenes-compra/{id}/confirmar-recepcion` | POST | EMPLEADO | N09 |
| Reportes | `/api/reportes/rotacion-productos` | GET | EMPLEADO | N10 |

## Cómo validar consistencia

1. Abrir Swagger: http://localhost:8080/swagger-ui/index.html
2. Confirmar que aparecen tags: Sucursales, Stock Sucursal, Promociones, Pedidos, Órdenes de Compra, Reportes
3. Ejecutar colección S9 en Postman
4. Comparar status codes y presencia de `_links` / `_embedded` donde aplique HAL
