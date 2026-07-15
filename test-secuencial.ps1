$base = "http://localhost:8080"
$results = @()
$suffix = Get-Random -Maximum 99999
$script:refreshToken = $null

function Get-LoginResponse([string]$u, [string]$p) {
    $b = @{ username = $u; password = $p } | ConvertTo-Json
    return Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body $b
}

function Get-Token([string]$u, [string]$p) {
    $r = Get-LoginResponse $u $p
    $t = if ($r.accessToken) { $r.accessToken } elseif ($r.token) { $r.token } else { $null }
    if (-not $t) { throw "Token vacio para $u" }
    if ($r.refreshToken) { $script:refreshToken = $r.refreshToken }
    return $t
}

function AuthHeader([string]$token) {
    return @{ Authorization = "Bearer $token" }
}

function Log([int]$n, [string]$name, [int]$status, [string]$exp, [string]$extra = "") {
    $ok = $status -in ($exp -split ',')
    $r = if ($ok) { "OK" } else { "FAIL" }
    $script:results += [PSCustomObject]@{ Num = $n; Name = $name; Status = $status; Expected = $exp; Result = $r }
    Write-Host "[$r] #$n $name -> $status (esperado: $exp) $extra"
}

function Try-Status([scriptblock]$fn) {
    try { & $fn | Out-Null; return 200 }
    catch { if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode.value__ }; throw }
}

function Try-StatusBody([scriptblock]$fn) {
    try { return @{ Status = 200; Body = (& $fn) } }
    catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode.value__ } else { throw }
        return @{ Status = $status; Body = $null }
    }
}

Write-Host "=== Prueba secuencial MiniMarket Semana 3 + 4 + 5 ===" -ForegroundColor Cyan

$s = (Invoke-WebRequest -Uri "$base/public/hola" -UseBasicParsing).StatusCode
Log 1 "Public - Hola" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -ErrorAction Stop }
Log 2 "Sin auth - Productos (publico)" $s "200"

$reg = @{
    username = "user_$suffix"
    password = "test1234"
    nombre = "Usuario"
    apellido = "Prueba"
    email = "user_$suffix@minimarket.cl"
    direccion = "Av. Test 123"
} | ConvertTo-Json
try { Invoke-RestMethod -Uri "$base/api/auth/register" -Method POST -ContentType "application/json" -Body $reg | Out-Null; $s = 201 } catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 3 "Registro nuevo cliente" $s "201,409"

$tC = Get-Token "cliente1" "cliente123"
Log 4 "Login CLIENTE" 200 "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -Headers (AuthHeader $tC) -ErrorAction Stop }
Log 5 "CLIENTE - Listar productos" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body '{"nombre":"X","precio":1,"stock":1,"categoria":{"id":1}}' -ErrorAction Stop }
Log 6 "CLIENTE - Crear producto (403)" $s "403"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Headers (AuthHeader $tC) -ErrorAction Stop }
Log 7 "CLIENTE - Inventario (403)" $s "403"

$tE = Get-Token "empleado1" "empleado123"
Log 8 "Login EMPLEADO" 200 "200"

$catName = "Cat_$suffix"
$cat = Invoke-RestMethod -Uri "$base/api/categorias" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"nombre`":`"$catName`"}"
$catId = $cat.id
Log 9 "EMPLEADO - Crear categoria" 200 "200" "catId=$catId"

$prod = Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"nombre`":`"Agua`",`"precio`":500,`"stock`":50,`"categoria`":{`"id`":$catId}}"
$prodId = $prod.id
Log 10 "EMPLEADO - Crear producto" 200 "200" "prodId=$prodId"

$tE = Get-Token "empleado1" "empleado123"
$invBody = "{`"producto`":{`"id`":$prodId},`"cantidad`":10,`"tipoMovimiento`":`"Entrada`",`"fechaMovimiento`":`"2026-06-01T12:00:00.000Z`"}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body $invBody -ErrorAction Stop }
Log 11 "EMPLEADO - Registrar inventario" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Headers (AuthHeader $tE) -ErrorAction Stop }
Log 12 "EMPLEADO - Listar inventario" $s "200"

try { Invoke-RestMethod -Uri "$base/api/productos/$prodId" -Method Delete -Headers (AuthHeader $tE) -ErrorAction Stop | Out-Null; $s = 204 } catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 13 "EMPLEADO - Eliminar producto (403)" $s "403"

$tG = Get-Token "gerente1" "gerente123"
Log 14 "Login GERENTE" 200 "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/usuarios" -Headers (AuthHeader $tG) -ErrorAction Stop }
Log 15 "GERENTE - Listar usuarios" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/usuarios" -Headers (AuthHeader $tC) -ErrorAction Stop }
Log 16 "CLIENTE - Listar usuarios (403)" $s "403"

$usuariosJson = (Invoke-WebRequest -Uri "$base/api/usuarios" -Headers (AuthHeader $tG) -UseBasicParsing).Content | ConvertFrom-Json
$clienteId = ($usuariosJson | Where-Object { $_.username -eq "cliente1" } | Select-Object -First 1).id
$empleadoId = ($usuariosJson | Where-Object { $_.username -eq "empleado1" } | Select-Object -First 1).id
$tC = Get-Token "cliente1" "cliente123"
$carBody = "{`"usuario`":{`"id`":$clienteId},`"producto`":{`"id`":$prodId},`"cantidad`":2}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/carrito" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $carBody -ErrorAction Stop }
Log 17 "CLIENTE - Agregar al carrito" $s "200"

$ventaBody = "{`"usuario`":{`"id`":$clienteId},`"fecha`":`"2026-06-01T14:00:00.000Z`"}"
try {
    $venta = Invoke-RestMethod -Uri "$base/api/ventas" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $ventaBody
    Log 18 "CLIENTE - Crear venta" 200 "200" "ventaId=$($venta.id)"
    $ventaId = $venta.id
} catch {
    Log 18 "CLIENTE - Crear venta" ([int]$_.Exception.Response.StatusCode.value__) "200"
    $ventaId = 1
}

$detBody = "{`"venta`":{`"id`":$ventaId},`"producto`":{`"id`":$prodId},`"cantidad`":1,`"precio`":500}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/detalle-ventas" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $detBody -ErrorAction Stop }
Log 19 "CLIENTE - Detalle venta" $s "200"

$tG = Get-Token "gerente1" "gerente123"
$prodOrfan = Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"nombre`":`"TempDelete`",`"precio`":99,`"stock`":1,`"categoria`":{`"id`":$catId}}"
try { Invoke-RestMethod -Uri "$base/api/productos/$($prodOrfan.id)" -Method Delete -Headers (AuthHeader $tG) -ErrorAction Stop | Out-Null; $s = 204 } catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 20 "GERENTE - Eliminar producto" $s "204" "prodId=$($prodOrfan.id)"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"cliente1","password":"wrong"}' -ErrorAction Stop }
Log 21 "Login credenciales invalidas (401)" $s "401"

Write-Host "`n--- BLOQUE B-Auth: Refresh tokens ---" -ForegroundColor Yellow
$loginCliente = Get-LoginResponse "cliente1" "cliente123"
$rt = $loginCliente.refreshToken
if (-not $rt) { throw "Refresh token no recibido en login" }

$refreshBody = @{ refreshToken = $rt } | ConvertTo-Json
$ref = Invoke-RestMethod -Uri "$base/api/auth/refresh" -Method POST -ContentType "application/json" -Body $refreshBody
Log 22 "Refresh token valido" 200 "200"
$rt = $ref.refreshToken

$logoutBody = @{ refreshToken = $rt } | ConvertTo-Json
try { Invoke-RestMethod -Uri "$base/api/auth/logout" -Method POST -ContentType "application/json" -Body $logoutBody | Out-Null; $s = 200 } catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 23 "Logout revoca refresh" $s "200"

try { Invoke-RestMethod -Uri "$base/api/auth/refresh" -Method POST -ContentType "application/json" -Body $logoutBody -ErrorAction Stop | Out-Null; $s = 200 } catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 24 "Refresh revocado (401)" $s "401"

Write-Host "`n--- BLOQUE V: Validaciones DTO ---" -ForegroundColor Yellow
$tE = Get-Token "empleado1" "empleado123"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body '{"nombre":"","precio":100,"stock":1,"categoria":{"id":1}}' -ErrorAction Stop }
Log 25 "Producto nombre vacio (400)" $s "400"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/ventas/registrar" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"usuarioId`":$empleadoId,`"items`":[]}" -ErrorAction Stop }
Log 26 "Venta registrar sin items (400)" $s "400"

$tC = Get-Token "cliente1" "cliente123"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/carrito" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body "{`"usuario`":{`"id`":$clienteId},`"producto`":{`"id`":1}}" -ErrorAction Stop }
Log 27 "Carrito sin cantidad (400)" $s "400"

Write-Host "`n--- BLOQUE E: Ventas Semana 4 ---" -ForegroundColor Yellow
$tE = Get-Token "empleado1" "empleado123"
$seedProd = Invoke-RestMethod -Uri "$base/api/productos/1" -Method GET
$ventaOkBody = @{ usuarioId = $empleadoId; items = @(@{ productoId = $seedProd.id; cantidad = 1 }) } | ConvertTo-Json -Depth 5
$r = Try-StatusBody { Invoke-RestMethod -Uri "$base/api/ventas/registrar" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body $ventaOkBody -ErrorAction Stop }
$totalOk = if ($r.Body -and [double]$r.Body.total -eq [double]$seedProd.precio) { 200 } else { 0 }
Log 28 "Registrar venta stock OK" $r.Status "200"
Log 29 "Total venta = precio x cantidad" $totalOk "200" "total=$($r.Body.total) precio=$($seedProd.precio)"

$ventaStockBody = @{ usuarioId = $empleadoId; items = @(@{ productoId = $seedProd.id; cantidad = 999999 }) } | ConvertTo-Json -Depth 5
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/ventas/registrar" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body $ventaStockBody -ErrorAction Stop }
Log 30 "Venta sin stock (409)" $s "409"

$ventaUserBody = @{ usuarioId = 99999; items = @(@{ productoId = $seedProd.id; cantidad = 1 }) } | ConvertTo-Json -Depth 5
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/ventas/registrar" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body $ventaUserBody -ErrorAction Stop }
Log 31 "Venta usuario inexistente (409)" $s "409"

$s = (Invoke-WebRequest -Uri "$base/public/hola" -UseBasicParsing).Headers["Content-Security-Policy"]
$hasUnsafe = if ($s -match "unsafe-inline") { 500 } else { 200 }
Log 32 "CSP sin unsafe-inline" $hasUnsafe "200"

Write-Host "`n--- BLOQUE F: Carrito e Inventario Semana 5 ---" -ForegroundColor Yellow
$tG = Get-Token "gerente1" "gerente123"
$usuariosJson = (Invoke-WebRequest -Uri "$base/api/usuarios" -Headers (AuthHeader $tG) -UseBasicParsing).Content | ConvertFrom-Json
$clienteId = ($usuariosJson | Where-Object { $_.username -eq "cliente1" } | Select-Object -First 1).id
$empleadoId = ($usuariosJson | Where-Object { $_.username -eq "empleado1" } | Select-Object -First 1).id
$seedProd = Invoke-RestMethod -Uri "$base/api/productos/1" -Method GET
$prodId = $seedProd.id

$tC = Get-Token "cliente1" "cliente123"
$carOkBody = "{`"usuario`":{`"id`":$clienteId},`"producto`":{`"id`":$prodId},`"cantidad`":1}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/carrito" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $carOkBody -ErrorAction Stop }
Log 33 "Carrito stock OK" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/carrito" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body "{`"usuario`":{`"id`":$clienteId},`"producto`":{`"id`":$prodId},`"cantidad`":999999}" -ErrorAction Stop }
Log 34 "Carrito sin stock (409)" $s "409"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/carrito" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body "{`"usuario`":{`"id`":$clienteId},`"producto`":{`"id`":99999},`"cantidad`":1}" -ErrorAction Stop }
Log 35 "Carrito producto inexistente (409)" $s "409"

$tE = Get-Token "empleado1" "empleado123"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"producto`":{`"id`":$prodId},`"cantidad`":5,`"tipoMovimiento`":`"Entrada`",`"fechaMovimiento`":`"2026-06-01T12:00:00.000Z`"}" -ErrorAction Stop }
Log 36 "Inventario Entrada (200)" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"producto`":{`"id`":$prodId},`"cantidad`":1,`"tipoMovimiento`":`"Salida`",`"fechaMovimiento`":`"2026-06-01T12:30:00.000Z`"}" -ErrorAction Stop }
Log 37 "Inventario Salida OK (200)" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"producto`":{`"id`":$prodId},`"cantidad`":1,`"tipoMovimiento`":`"Ajuste`",`"fechaMovimiento`":`"2026-06-01T12:00:00.000Z`"}" -ErrorAction Stop }
Log 38 "Inventario tipo invalido (409)" $s "409"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"producto`":{`"id`":$prodId},`"cantidad`":999999,`"tipoMovimiento`":`"Salida`",`"fechaMovimiento`":`"2026-06-01T12:00:00.000Z`"}" -ErrorAction Stop }
Log 39 "Inventario Salida sin stock (409)" $s "409"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"producto`":{`"id`":$prodId},`"cantidad`":5}" -ErrorAction Stop }
Log 40 "Inventario sin tipoMovimiento (400)" $s "400"

Write-Host "`n=== RESUMEN ===" -ForegroundColor Cyan
$results | Format-Table -AutoSize
$failed = @($results | Where-Object { $_.Result -eq "FAIL" }).Count
Write-Host "Pasaron: $($results.Count - $failed) / $($results.Count) | Fallaron: $failed"
if ($failed -gt 0) { exit 1 }
