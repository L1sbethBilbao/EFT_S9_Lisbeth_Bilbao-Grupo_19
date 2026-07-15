# Guía — Importar y probar Semana 8 (HATEOAS)

## 1. Levantar la aplicación

```powershell
cd Exp3_S8_Lisbeth_Bilbao_Grupo19
.\mvnw.cmd spring-boot:run
```

URLs:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 2. Importar en Postman

| Paso | Archivo |
|------|---------|
| Environment | `S8_03_Environment_local.json` |
| OpenAPI (opcional) | `S8_01_OpenAPI_importar.json` |
| Colección HATEOAS | `S8_02_Coleccion_endpoints_hateoas.json` |

Seleccionar el environment **Semana 8 — Local** arriba a la derecha.

## 3. Orden de ejecución

1. **S00** — Login GERENTE
2. **S00b** — Login CLIENTE
3. **S00c** — Login EMPLEADO
4. **H01** a **H03** — Productos (público + GERENTE)
5. **H05** — POST carrito (antes de H04)
6. **H04** — GET carrito paginado
7. **H06**, **H07** — Inventario (EMPLEADO)
8. **H08**, **H09** — Usuarios (GERENTE)

Cada request incluye tests Postman que validan `_links`, `_embedded` y `page`.

## 4. Verificar en Swagger UI

1. Abrir GET `/api/productos` → Execute con `page=0`, `size=5`
2. Confirmar en la respuesta: `_embedded`, `page`, `_links`
3. Abrir GET `/api/productos/{id}` → confirmar `_links.self` y `_links.productos`

## 5. Regenerar evidencias desde tests

```powershell
.\mvnw.cmd verify
```

Genera/actualiza:
- `evidencia/semana8/resultados-validacion.md`
- `postman/S8_01_OpenAPI_importar.json`
