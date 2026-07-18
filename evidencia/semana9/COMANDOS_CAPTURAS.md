# Comandos para capturas de evidencia (EFT S9)

Guía corta en PowerShell para levantar MiniMarket Plus, generar evidencias de tests/JaCoCo y capturar Swagger/Postman HAL para el informe.

## Carpeta de trabajo

```powershell
cd C:\Users\lisbe\OneDrive\Escritorio\S9_backend\EFT_S9_Lisbeth_Bilbao-Grupo_19
```

---

## 1. Arrancar la API (Swagger + Postman / HATEOAS)

```powershell
.\mvnw.cmd spring-boot:run
```

Esperar a que la aplicación arranque. Luego abrir en el navegador:

| URL | Evidencia |
|-----|-----------|
| http://localhost:8080/swagger-ui/index.html | **E-DOC-OAS-01** |
| http://localhost:8080/v3/api-docs | Contrato OpenAPI en runtime |

### Usuarios demo (login Swagger/Postman)

| Usuario | Contraseña |
|---------|------------|
| `cliente1` | `cliente123` |
| `empleado1` | `empleado123` |
| `gerente1` | `gerente123` |

### Postman (con la app corriendo)

1. Importar `postman/S9_03_Environment_local.json`
2. Importar `postman/S9_02_Coleccion_endpoints_EFT.json`
3. Ejecutar y capturar el Body:

| Request | Qué mirar | Evidencia |
|---------|-----------|-----------|
| **H02** o **H13** | `_links` | **E-DOC-HAT-01** |
| **H01** o **H12** | `_embedded` + `page` | **E-DOC-HAT-02** |
| **S00b** (login cliente) + **N03** (pedido RETIRO) | `_links` del pedido | **E-DOC-HAT-03** |

Guía detallada: [`../../postman/S9_GUIA_importar_y_probar.md`](../../postman/S9_GUIA_importar_y_probar.md)

### Sin comando (explorador)

Abrir `evidencia/semana9/openapi.json` → captura **E-DOC-OAS-02**

Para detener la app: `Ctrl+C` en la terminal.

---

## 2. Pruebas + cobertura

Usar otra terminal (o detener la app antes). Desde la carpeta del proyecto:

```powershell
.\mvnw.cmd verify
```

Capturar la consola con `BUILD SUCCESS` → **E-TEST-01**

Abrir el reporte JaCoCo:

```powershell
start target\site\jacoco\index.html
```

→ captura **E-TEST-COV-01**

---

## Orden práctico recomendado

1. `.\mvnw.cmd verify` + captura JaCoCo  
2. `.\mvnw.cmd spring-boot:run`  
3. Swagger + Postman HAL  
4. Explorador: `openapi.json`

```text
cd proyecto
   → mvnw verify + captura JaCoCo
   → mvnw spring-boot:run
        → Capturar Swagger
        → Capturar Postman HAL
   → Capturar openapi.json en carpeta
```

---

## Checklist rápido de capturas (mínimo evaluación)

| ID | Qué capturar |
|----|--------------|
| E-ENV-01 | `java -version` + `.\mvnw.cmd -version` |
| E-INT-01 | Árbol `com.minimarket` en el IDE |
| E-INT-02 | Collection Runner (resumen + scrolls) — **cubre Postman/HATEOAS/módulos** |
| E-POM-01 | `pom.xml` dependencies |
| E-SEG-01 | Login JWT (Body con token) |
| E-SEG-03 | 401/403 por rol |
| E-TEST-01 | `mvn verify` BUILD SUCCESS |
| E-TEST-COV-01 | JaCoCo HTML |
| E-DOC-OAS-01 | Swagger UI |
| E-DOC-OAS-02 | `openapi.json` en carpeta |
| E-CODE-02 | Assembler/Controller con EntityModel |

No hace falta capturar por separado MS/INV/VEN/HAT: van dentro de **E-INT-02**.

Ver también [`resultados-validacion.md`](./resultados-validacion.md).
