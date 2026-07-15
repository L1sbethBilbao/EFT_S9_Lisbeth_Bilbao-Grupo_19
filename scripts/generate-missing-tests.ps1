$projectRoot = Split-Path $PSScriptRoot -Parent
$mainRoot = Join-Path $projectRoot "src\main\java\com\minimarket"
$testRoot = Join-Path $projectRoot "src\test\java\com\minimarket"

$controllerConfig = @{
    "HolaMundoController" = @{ Path = "/public/hola"; Role = $null }
    "ProductoController" = @{ Path = "/api/productos"; Role = $null }
    "CategoriaController" = @{ Path = "/api/categorias"; Role = $null }
    "InventarioController" = @{ Path = "/api/inventario"; Role = "EMPLEADO" }
    "CarritoController" = @{ Path = "/api/carrito"; Role = "CLIENTE" }
    "DetalleVentaController" = @{ Path = "/api/detalle-ventas"; Role = "CLIENTE" }
    "AuditLogController" = @{ Path = "/api/audit-logs"; Role = "GERENTE" }
    "DataRetentionController" = @{ Path = "/api/admin/retention/run"; Role = "GERENTE"; Method = "POST" }
}

function Get-PackageFromPath([string]$relPath) {
    $dir = Split-Path $relPath -Parent
    if ([string]::IsNullOrEmpty($dir)) { return "com.minimarket" }
    return "com.minimarket." + ($dir -replace '\\', '.')
}

function Get-TestContent([string]$sourcePath, [string]$className, [string]$pkg, [string]$relDir) {
    $src = Get-Content $sourcePath -Raw

    if ($src -match 'public interface ') {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ${className}Test {

    @Test
    void esInterfazJava() {
        assertThat(${className}.class.isInterface()).isTrue();
    }
}
"@
    }

    if ($src -match 'public enum ') {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ${className}Test {

    @Test
    void enumTieneConstantes() {
        assertThat(${className}.values().length).isGreaterThan(0);
    }
}
"@
    }

    if ($src -match '@interface ') {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ${className}Test {

    @Test
    void esAnotacionRuntime() {
        assertThat(${className}.class.isAnnotation()).isTrue();
    }
}
"@
    }

    if ($src -match 'extends RuntimeException' -or $src -match 'extends Exception') {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ${className}Test {

    @Test
    void conservaMensaje() {
        assertThat(new ${className}("error-demo").getMessage()).isEqualTo("error-demo");
    }
}
"@
    }

    if ($controllerConfig.ContainsKey($className)) {
        $cfg = $controllerConfig[$className]
        $method = if ($cfg.Method) { $cfg.Method } else { "GET" }
        $roleImport = ""
        $roleAnnot = ""
        if ($cfg.Role) {
            $roleImport = "import org.springframework.security.test.context.support.WithMockUser;`n"
            $roleAnnot = "`n    @WithMockUser(roles = `"$($cfg.Role)`")"
        }
        $perform = if ($method -eq "POST") {
            "post(`"$($cfg.Path)`")"
        } else {
            "get(`"$($cfg.Path)`")"
        }
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
${roleImport}import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ${className}Test {

    @Autowired
    private MockMvc mockMvc;

    @Test${roleAnnot}
    void endpointRespondeCorrectamente() throws Exception {
        mockMvc.perform($perform)
                .andExpect(status().isOk());
    }
}
"@
    }

    if ($relDir -match '\\service\\impl\\') {
        $repoField = switch ($className) {
            "ProductoServiceImpl" { "ProductoRepository" }
            "CategoriaServiceImpl" { "CategoriaRepository" }
            "InventarioServiceImpl" { "InventarioRepository" }
            "CarritoServiceImpl" { "CarritoRepository" }
            "DetalleVentaServiceImpl" { "DetalleVentaRepository" }
            "RolServiceImpl" { "RolRepository" }
            default { $null }
        }
        if ($repoField) {
            $repoPkg = "com.minimarket.repository"
            $entity = $className -replace 'ServiceImpl$',''
            return @"
package $pkg;

import com.minimarket.entity.$entity;
import ${repoPkg}.${repoField};
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ${className}Test {

    @Mock
    private ${repoField} repository;

    @InjectMocks
    private ${className} service;

    @Test
    void findAll_retornaLista() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.findAll()).isEmpty();
    }
}
"@
        }
    }

    if ($relDir -match '\\mapper\\') {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ${className}Test {

    @Autowired
    private ${className} mapper;

    @Test
    void mapperEstaRegistradoEnContexto() {
        assertThat(mapper).isNotNull();
    }
}
"@
    }

    if ($relDir -match '\\repository\\') {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ${className}Test {

    @Autowired
    private ${className} repository;

    @Test
    void repositorioEstaDisponible() {
        assertThat(repository).isNotNull();
        assertThat(repository.findAll()).isNotNull();
    }
}
"@
    }

    if ($className -eq "SecurityRoles") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityRolesTest {

    @Test
    void toAuthority_agregaPrefijoRole() {
        assertThat(SecurityRoles.toAuthority(SecurityRoles.CLIENTE)).isEqualTo("ROLE_CLIENTE");
    }
}
"@
    }

    if ($className -eq "SecurityExpressions") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityExpressionsTest {

    @Test
    void constantesNoEstanVacias() {
        assertThat(SecurityExpressions.AUTENTICADO).isNotBlank();
        assertThat(SecurityExpressions.SOLO_GERENTE).isNotBlank();
    }
}
"@
    }

    if ($className -eq "GlobalExceptionHandler") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_retorna409() {
        ResponseEntity<?> response = handler.handleIllegalArgument(new IllegalArgumentException("conflicto"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
"@
    }

    if ($className -eq "CustomUserDetailsService") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_usuarioDemoExiste() {
        UserDetails user = customUserDetailsService.loadUserByUsername("cliente1");
        assertThat(user.getUsername()).isEqualTo("cliente1");
    }
}
"@
    }

    if ($className -eq "DataInitializer") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataInitializerTest {

    @Autowired
    private DataInitializer dataInitializer;

    @Test
    void beanEstaRegistrado() {
        assertThat(dataInitializer).isNotNull();
    }
}
"@
    }

    if ($relDir -match '\\security\\(filter|handler|listener|monitor)\\' -or $className -eq "AuditService" -or $className -eq "SuspiciousActivityService") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ${className}Test {

    @Autowired
    private ${className} bean;

    @Test
    void componenteEstaRegistrado() {
        assertThat(bean).isNotNull();
    }
}
"@
    }

    if ($className -eq "DataRetentionProperties") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataRetentionPropertiesTest {

    @Autowired
    private DataRetentionProperties properties;

    @Test
    void propiedadesCargadas() {
        assertThat(properties.getInactiveDays()).isPositive();
    }
}
"@
    }

    if ($className -eq "TokenPairResponse") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TokenPairResponseTest {

    @Test
    void accessAndRefresh_incluyeTokens() {
        TokenPairResponse response = TokenPairResponse.accessAndRefresh(
                "access", "refresh", 900000L, "user1", List.of("CLIENTE"));
        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getToken()).isEqualTo("access");
    }
}
"@
    }

    if ($className -eq "RolDTO") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RolDTOTest {

    @Test
    void constructorAsignaCampos() {
        RolDTO dto = new RolDTO(1L, "CLIENTE");
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("CLIENTE");
    }
}
"@
    }

    if ($className -eq "IdRefDTO") {
        return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdRefDTOTest {

    @Test
    void constructorAsignaId() {
        IdRefDTO dto = new IdRefDTO(10L);
        assertThat(dto.getId()).isEqualTo(10L);
    }
}
"@
    }

    # Default POJO / model / entity / dto / properties
    return @"
package $pkg;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ${className}Test {

    @Test
    void instanciaSeCreaCorrectamente() {
        assertThat(new ${className}()).isNotNull();
    }
}
"@
}

$created = 0
Get-ChildItem -Recurse $mainRoot -Filter "*.java" | ForEach-Object {
    $relFromMinimarket = $_.FullName.Substring($mainRoot.Length + 1)
    $testRel = $relFromMinimarket -replace '\.java$', 'Test.java'
    $testPath = Join-Path $testRoot $testRel
    if (Test-Path $testPath) { return }

    $className = $_.BaseName
    $pkg = Get-PackageFromPath $relFromMinimarket
    $relDir = Split-Path $_.FullName -Parent

    $content = Get-TestContent $_.FullName $className $pkg $relDir
    $dir = Split-Path $testPath -Parent
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    [System.IO.File]::WriteAllText($testPath, $content, (New-Object System.Text.UTF8Encoding $false))
    $created++
    Write-Host "Created: $testRel"
}

Write-Host "`nTotal created: $created"
