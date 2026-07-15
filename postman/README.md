# Carpeta Postman — Semana 8

Solo contiene los archivos necesarios para la entrega de Semana 8.

## Qué importar en Postman

| Orden | Archivo | Tipo en Postman | Para qué |
|-------|---------|-----------------|----------|
| 1 | **`S8_03_Environment_local.json`** | Environment | URL base y usuarios demo |
| 2 | **`S8_02_Coleccion_endpoints_hateoas.json`** | Collection | Probar H01–H09 (HATEOAS) |
| 3 | **`S8_01_OpenAPI_importar.json`** | OpenAPI (opcional) | Contrato JSON exportado |

## Guías (no se importan a Postman)

- `S8_GUIA_importar_y_probar.md` — pasos detallados
- `S8_CONSISTENCIA_swagger_postman.md` — tabla Swagger vs Postman

## Orden de ejecución

1. Levantar app: `.\mvnw.cmd spring-boot:run`
2. Import → `S8_03_Environment_local.json` → seleccionarlo arriba a la derecha
3. Import → `S8_02_Coleccion_endpoints_hateoas.json`
4. Ejecutar **S00**, **S00b**, **S00c** (logins)
5. Ejecutar **H01** a **H09** (**H05 antes de H04**)

## Regenerar OpenAPI

```powershell
cd Exp3_S8_Lisbeth_Bilbao_Grupo19
.\mvnw.cmd test "-Dtest=OpenApiDocsExportTest,HateoasSemana8EvidenciaTest"
```

Actualiza `S8_01_OpenAPI_importar.json` y `evidencia/semana8/resultados-validacion.md`.
