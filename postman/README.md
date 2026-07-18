# Carpeta Postman — EFT Semana 9

## Qué importar

| Orden | Archivo | Tipo | Para qué |
|-------|---------|------|----------|
| 1 | **`S9_03_Environment_local.json`** | Environment | URL, usuarios demo, tokens, `sucursalId`, `pedidoId` |
| 2 | **`S9_02_Coleccion_endpoints_EFT.json`** | Collection | Logins + HATEOAS H01–H15 + negocio N01–N11 |
| 3 | **`S9_01_OpenAPI_importar.json`** | OpenAPI (opcional) | Contrato JSON exportado (`/v3/api-docs`) |

## Guía paso a paso

- **`S9_GUIA_importar_y_probar.md`** — orden exacto de pruebas HATEOAS + negocio
- **`S9_CONSISTENCIA_swagger_postman.md`** — alineación Swagger ↔ Postman

## Orden rápido de ejecución

1. Levantar app: `.\mvnw.cmd spring-boot:run`
2. Import environment `S9_03` → seleccionarlo
3. Import collection `S9_02`
4. Ejecutar carpeta **00 — Logins** (S00, S00b, S00c)
5. Carpeta **01** H01–H09 (**H05 antes de H04**)
6. Carpeta **02** H10–H15 (HATEOAS S9)
7. Carpeta **03** N01–N11 (negocio MiniMarket Plus)

O usar **Collection Runner** sobre toda la colección (evidencia E-INT-02).

## Contenido de la colección S9

| Carpeta | Requests | Enfoque |
|---------|----------|---------|
| 00 — Logins | S00, S00b, S00c | JWT gerente / cliente / empleado |
| 01 — HATEOAS base | H01–H09 | Productos, carrito, inventario, usuarios |
| 02 — HATEOAS S9 | H10–H15 | Categorías, ventas, sucursales, promociones, stock |
| 03 — Negocio S9 | N01–N11 | Disponibilidad, pedidos RETIRO/DESPACHO, órdenes compra, reportes, promociones |

## Regenerar OpenAPI

```powershell
cd EFT_S9_Lisbeth_Bilbao-Grupo_19
.\mvnw.cmd test "-Dtest=OpenApiDocsExportTest"
```

Actualiza `postman/S9_01_OpenAPI_importar.json`. Copiar también a `evidencia/semana9/openapi.json` si se regeneró el contrato.
