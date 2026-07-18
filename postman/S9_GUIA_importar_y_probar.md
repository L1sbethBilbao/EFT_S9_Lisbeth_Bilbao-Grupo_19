# Guía — Importar y probar EFT Semana 9

## 1. Levantar la aplicación

```powershell
cd "C:\Users\lisbe\OneDrive\Escritorio\S9_backend\EFT_S9_Lisbeth_Bilbao-Grupo_19"
.\mvnw.cmd spring-boot:run
```

URLs:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 2. Importar en Postman

| Orden | Archivo | Tipo en Postman |
|------|---------|-----------------|
| 1 | `S9_03_Environment_local.json` | Environment |
| 2 | `S9_02_Coleccion_endpoints_EFT.json` | Collection |
| 3 | `S9_01_OpenAPI_importar.json` (opcional) | OpenAPI |

Seleccionar el environment **MiniMarket Plus - Local (EFT Semana 9)** arriba a la derecha.

## 3. Usuarios demo

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| cliente1 | cliente123 | CLIENTE |
| empleado1 | empleado123 | EMPLEADO |
| gerente1 | gerente123 | GERENTE |

## 4. Orden exacto de pruebas

### Paso A — Logins (carpeta `00 — Logins`)

Ejecutar en este orden:

1. **S00** — Login GERENTE  
2. **S00b** — Login CLIENTE  
3. **S00c** — Login EMPLEADO  

Cada uno debe devolver **200** y guardar el token (revisar **Test Results** en verde).

---

### Paso B — HATEOAS base H01–H09 (carpeta `01`)

| Orden | Request | Rol | Qué validar |
|-------|---------|-----|-------------|
| 1 | **H01** GET productos paginado | Público | `_embedded`, `page`, `_links` |
| 2 | **H02** GET producto por id | Público | `_links.self`, `_links.productos` |
| 3 | **H03** POST producto | GERENTE | `id`, `_links.self` |
| 4 | **H05** POST carrito | CLIENTE | `_links.self`, `_links.carrito` |
| 5 | **H04** GET carrito paginado | CLIENTE | `_embedded`, `page` (**después de H05**) |
| 6 | **H06** GET inventario | EMPLEADO | `_embedded`, `page` |
| 7 | **H07** POST inventario | EMPLEADO | `_links.self`, `_links.inventario` |
| 8 | **H08** GET usuarios | GERENTE | `_embedded`, `page.size=5` |
| 9 | **H09** GET usuario por id | GERENTE | `_links.self`, `_links.usuarios` |

**Importante:** H05 antes de H04.

---

### Paso C — HATEOAS S9 (carpeta `02`)

| Orden | Request | Rol | Qué validar |
|-------|---------|-----|-------------|
| 10 | **H10** GET categorías paginado | Público | `_links`, `page` |
| 11 | **H11** GET ventas paginado | CLIENTE | `_links`, `page` |
| 12 | **H12** GET sucursales paginado | Público | `_embedded`, guarda `sucursalId` |
| 13 | **H13** GET sucursal por id | Público | `_links.self` |
| 14 | **H14** GET promociones paginado | Público | `_links`, `page` |
| 15 | **H15** GET stock-sucursal | EMPLEADO | `_embedded`, `page` |

---

### Paso D — Negocio MiniMarket Plus (carpeta `03`)

| Orden | Request | Rol | Qué valida del caso |
|-------|---------|-----|---------------------|
| 16 | **N01** Disponibilidad por sucursal | Público | Stock en tiempo real por sucursal |
| 17 | **N02** Promociones activas | Público | Ofertas centralizadas |
| 18 | **N03** Pedido RETIRO | CLIENTE | Pedido retiro en tienda + HATEOAS |
| 19 | **N04** Pedido DESPACHO | CLIENTE | Pedido despacho a domicilio |
| 20 | **N05** Listar pedidos | CLIENTE | Listado HAL paginado |
| 21 | **N06** Actualizar estado pedido | EMPLEADO | Flujo operativo del pedido |
| 22 | **N07** Generar órdenes automáticas | EMPLEADO | Órdenes al stock mínimo |
| 23 | **N08** Listar órdenes de compra | EMPLEADO | Consulta órdenes |
| 24 | **N09** Confirmar recepción | EMPLEADO | Reposición de stock (si hay orden) |
| 25 | **N10** Reporte rotación | EMPLEADO | Más/menos vendidos |
| 26 | **N11** Crear promoción | GERENTE | Gestión centralizada de ofertas |

**Notas:**
- N03 guarda `pedidoId` para N06.
- N07/N08 guardan `ordenCompraId` para N09.
- Si N07 devuelve `[]`, no hay stock bajo mínimo (es válido). Puedes bajar stock con más pedidos o revisar N09 solo si hay `ordenCompraId`.

---

## 5. Cómo ejecutar toda la colección de una vez

1. Click derecho sobre la colección **EFT Semana 9 — HATEOAS + Negocio MiniMarket Plus**
2. **Run collection**
3. Verificar que el environment S9 esté seleccionado
4. **Run**
5. Revisar que todos los tests queden en verde

## 6. Evidencias sugeridas para el informe

Capturar pantallas de:
1. S00 login + token
2. H01 productos HAL (`_embedded` / `_links`)
3. H12 sucursales
4. N01 disponibilidad
5. N03 pedido RETIRO
6. N04 pedido DESPACHO
7. N07 órdenes automáticas
8. N10 reporte rotación
9. Swagger UI con tags S9

## 7. Regenerar OpenAPI exportado

```powershell
.\mvnw.cmd test "-Dtest=OpenApiDocsExportTest"
```

Actualiza `S9_01_OpenAPI_importar.json`. Opcional: copiar a `evidencia/semana9/openapi.json`.

## 8. Archivos de esta carpeta (solo Semana 9)

- `S9_01_OpenAPI_importar.json`
- `S9_02_Coleccion_endpoints_EFT.json`
- `S9_03_Environment_local.json`
- `S9_GUIA_importar_y_probar.md`
- `S9_CONSISTENCIA_swagger_postman.md`
- `README.md`
