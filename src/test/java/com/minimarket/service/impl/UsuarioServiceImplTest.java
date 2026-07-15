package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.UsuarioIncompletoException;
import com.minimarket.mapper.UsuarioMapper;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.service.impl.UsuarioServiceImpl;
import com.minimarket.support.TestFixtures;
import com.minimarket.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioCompleto;

    @BeforeEach
    void setUp() {
        usuarioCompleto = TestFixtures.usuarioClienteCompleto();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Usuario Con Todos Los Campos THEN Es Valido")
    void givenDefaultContext_whenUsuarioConTodosLosCampos_thenEsValido() {
        assertThat(usuarioService.tieneDatosCompletos(usuarioCompleto)).isTrue();
        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Completo WHEN Validar Datos Completos THEN No Lanza Excepcion")
    void givenUsuarioCompleto_whenValidarDatosCompletos_thenNoLanzaExcepcion() {
        usuarioService.validarDatosCompletos(usuarioCompleto);
    }

    @Test
    @DisplayName("GIVEN Usuario Con Rol Valido WHEN Validar Puede Registrar Venta THEN No Lanza Excepcion")
    void givenUsuarioConRolValido_whenValidarPuedeRegistrarVenta_thenNoLanzaExcepcion() {
        Rol empleado = new Rol();
        empleado.setNombre(SecurityRoles.EMPLEADO);
        usuarioCompleto.setRoles(new HashSet<>(Set.of(empleado)));
        usuarioService.validarPuedeRegistrarVenta(usuarioCompleto);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Usuario Sin Email THEN Lanza Excepcion")
    void givenDefaultContext_whenUsuarioSinEmail_thenLanzaExcepcion() {
        usuarioCompleto.setEmail(null);
        assertThatThrownBy(() -> usuarioService.validarDatosCompletos(usuarioCompleto))
                .isInstanceOf(UsuarioIncompletoException.class);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Usuario Sin Apellido THEN Lanza Excepcion")
    void givenDefaultContext_whenUsuarioSinApellido_thenLanzaExcepcion() {
        usuarioCompleto.setApellido(" ");
        assertThatThrownBy(() -> usuarioService.validarDatosCompletos(usuarioCompleto))
                .isInstanceOf(UsuarioIncompletoException.class);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Usuario Sin Direccion THEN Lanza Excepcion")
    void givenDefaultContext_whenUsuarioSinDireccion_thenLanzaExcepcion() {
        usuarioCompleto.setDireccion(null);
        assertThatThrownBy(() -> usuarioService.validarDatosCompletos(usuarioCompleto))
                .isInstanceOf(UsuarioIncompletoException.class);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Usuario Sin Rol Valido THEN No Puede Registrar Venta")
    void givenDefaultContext_whenUsuarioSinRolValido_thenNoPuedeRegistrarVenta() {
        usuarioCompleto.setRoles(new HashSet<>());
        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isFalse();
        assertThatThrownBy(() -> usuarioService.validarPuedeRegistrarVenta(usuarioCompleto))
                .isInstanceOf(UsuarioIncompletoException.class);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find All THEN Retorna Lista Sanitizada")
    void givenDefaultContext_whenFindAll_thenRetornaListaSanitizada() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioCompleto));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Username THEN Retorna Usuario")
    void givenDefaultContext_whenFindByUsername_thenRetornaUsuario() {
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(usuarioCompleto));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.findByUsername("cliente1")).isPresent();
    }

    @Test
    @DisplayName("GIVEN Nuevo Usuario WHEN Save THEN Codifica Password")
    void givenNuevoUsuario_whenSave_thenCodificaPassword() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("nuevo2");
        nuevo.setPassword("plain");
        nuevo.setNombre("N");
        nuevo.setApellido("A");
        nuevo.setEmail("n@t.cl");
        nuevo.setDireccion("D");

        when(passwordEncoder.encode("plain")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario saved = usuarioService.save(nuevo);

        assertThat(saved.getPassword()).startsWith("$2a$");
    }

    @Test
    @DisplayName("GIVEN Usuario Existente WHEN Save THEN Preserva Password Si Viene Vacio")
    void givenUsuarioExistente_whenSave_thenPreservaPasswordSiVieneVacio() {
        Usuario existing = new Usuario();
        existing.setId(1L);
        existing.setPassword("$2a$existing");
        existing.setTotpSecret("secret");
        existing.setMfaEnabled(true);

        Usuario update = new Usuario();
        update.setId(1L);
        update.setUsername("cliente1");
        update.setPassword("");
        update.setNombre("Lisbeth");
        update.setApellido("Bilbao");
        update.setEmail("cliente1@minimarket.cl");
        update.setDireccion("Av. Principal 100");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario saved = usuarioService.save(update);

        assertThat(saved.getPassword()).isEqualTo("$2a$existing");
        assertThat(saved.getTotpSecret()).isEqualTo("secret");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Delete By Id THEN Invoca Repositorio")
    void givenDefaultContext_whenDeleteById_thenInvocaRepositorio() {
        usuarioService.deleteById(5L);
        verify(usuarioRepository).deleteById(5L);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Save From Dto THEN Asigna Roles Desde Repositorio")
    void givenDefaultContext_whenSaveFromDto_thenAsignaRolesDesdeRepositorio() {
        com.minimarket.dto.usuario.UsuarioRequestDTO dto = new com.minimarket.dto.usuario.UsuarioRequestDTO();
        dto.setUsername("nuevo");
        dto.setPassword("pass1234");
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setEmail("ana@test.cl");
        dto.setDireccion("Calle 1");
        dto.setRoleNames(java.util.List.of(SecurityRoles.CLIENTE));

        Rol cliente = new Rol();
        cliente.setNombre(SecurityRoles.CLIENTE);
        Usuario entity = new Usuario();
        entity.setUsername("nuevo");

        when(usuarioMapper.toEntity(dto)).thenReturn(entity);
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE)).thenReturn(java.util.Optional.of(cliente));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario saved = usuarioService.saveFromDto(dto);

        assertThat(saved.getRoles()).hasSize(1);
        verify(rolRepository).findByNombre(SecurityRoles.CLIENTE);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Find By Id THEN Usuario Tiene Rol Asociado")
    void givenDefaultContext_whenFindById_thenUsuarioTieneRolAsociado() {
        Rol gerente = new Rol();
        gerente.setId(3L);
        gerente.setNombre(SecurityRoles.GERENTE);

        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("gerente1");
        u.setNombre("Gerente");
        u.setApellido("Demo");
        u.setEmail("gerente1@minimarket.cl");
        u.setDireccion("Av. Principal 100");
        u.setRoles(new HashSet<>(Set.of(gerente)));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario found = usuarioService.findById(1L).orElseThrow();
        assertThat(found.getRoles()).extracting(Rol::getNombre).contains(SecurityRoles.GERENTE);
    }

    @Test
    @DisplayName("GIVEN No Existe WHEN Find By Id THEN Retorna Vacio")
    void givenNoExiste_whenFindById_thenRetornaVacio() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(usuarioService.findById(99L)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN Usuario Null WHEN Tiene Datos Completos THEN Retorna False")
    void givenUsuarioNull_whenTieneDatosCompletos_thenRetornaFalse() {
        assertThat(usuarioService.tieneDatosCompletos(null)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Sin Nombre WHEN Tiene Datos Completos THEN Retorna False")
    void givenSinNombre_whenTieneDatosCompletos_thenRetornaFalse() {
        usuarioCompleto.setNombre(null);
        assertThat(usuarioService.tieneDatosCompletos(usuarioCompleto)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Null WHEN Puede Registrar Venta THEN Retorna False")
    void givenUsuarioNull_whenPuedeRegistrarVenta_thenRetornaFalse() {
        assertThat(usuarioService.puedeRegistrarVenta(null)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Roles Null WHEN Puede Registrar Venta THEN Retorna False")
    void givenRolesNull_whenPuedeRegistrarVenta_thenRetornaFalse() {
        usuarioCompleto.setRoles(null);
        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Empleado Y Gerente WHEN Puede Registrar Venta THEN Retorna True")
    void givenEmpleadoYGerente_whenPuedeRegistrarVenta_thenRetornaTrue() {
        Rol empleado = new Rol();
        empleado.setNombre(SecurityRoles.EMPLEADO);
        usuarioCompleto.setRoles(new HashSet<>(Set.of(empleado)));
        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isTrue();

        Rol gerente = new Rol();
        gerente.setNombre(SecurityRoles.GERENTE);
        usuarioCompleto.setRoles(new HashSet<>(Set.of(gerente)));
        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isTrue();
    }

    @Test
    @DisplayName("GIVEN Password Ya Hasheado WHEN Save THEN No Re Codifica")
    void givenPasswordYaHasheado_whenSave_thenNoReCodifica() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("hash1");
        nuevo.setPassword("$2b$alreadyhashed");
        nuevo.setNombre("N");
        nuevo.setApellido("A");
        nuevo.setEmail("h@t.cl");
        nuevo.setDireccion("D");

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario saved = usuarioService.save(nuevo);

        assertThat(saved.getPassword()).isEqualTo("$2b$alreadyhashed");
        assertThat(saved.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("GIVEN Sin Roles WHEN Save From Dto THEN Guarda Usuario")
    void givenSinRoles_whenSaveFromDto_thenGuardaUsuario() {
        com.minimarket.dto.usuario.UsuarioRequestDTO dto = new com.minimarket.dto.usuario.UsuarioRequestDTO();
        dto.setUsername("simple");
        Usuario entity = new Usuario();
        entity.setUsername("simple");

        when(usuarioMapper.toEntity(dto)).thenReturn(entity);
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.saveFromDto(dto).getUsername()).isEqualTo("simple");
    }

    @Test
    @DisplayName("GIVEN Rol Inexistente WHEN Save From Dto THEN Lanza Excepcion")
    void givenRolInexistente_whenSaveFromDto_thenLanzaExcepcion() {
        com.minimarket.dto.usuario.UsuarioRequestDTO dto = new com.minimarket.dto.usuario.UsuarioRequestDTO();
        dto.setRoleNames(List.of("INVALIDO"));
        when(usuarioMapper.toEntity(dto)).thenReturn(new Usuario());
        when(rolRepository.findByNombre("INVALIDO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.saveFromDto(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("GIVEN Usuario Existente Password Null WHEN Save THEN Preserva Password")
    void givenUsuarioExistentePasswordNull_whenSave_thenPreservaPassword() {
        Usuario existing = new Usuario();
        existing.setId(1L);
        existing.setPassword("$2a$existing");

        Usuario update = new Usuario();
        update.setId(1L);
        update.setUsername("cliente1");
        update.setPassword(null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(update).getPassword()).isEqualTo("$2a$existing");
    }

    @Test
    @DisplayName("GIVEN Nuevo Con Last Login At WHEN Save THEN No Sobrescribe Fecha")
    void givenNuevoConLastLoginAt_whenSave_thenNoSobrescribeFecha() {
        LocalDateTime original = LocalDateTime.now().minusDays(2);
        Usuario nuevo = new Usuario();
        nuevo.setUsername("nuevo3");
        nuevo.setLastLoginAt(original);

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(nuevo).getLastLoginAt()).isEqualTo(original);
    }

    @Test
    @DisplayName("GIVEN Password Null WHEN Save THEN No Intenta Codificar")
    void givenPasswordNull_whenSave_thenNoIntentaCodificar() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("nuevo4");
        nuevo.setPassword(null);

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(nuevo).getPassword()).isNull();
    }

    @Test
    @DisplayName("GIVEN Role Names Null WHEN Save From Dto THEN No Asigna Roles")
    void givenRoleNamesNull_whenSaveFromDto_thenNoAsignaRoles() {
        com.minimarket.dto.usuario.UsuarioRequestDTO dto = new com.minimarket.dto.usuario.UsuarioRequestDTO();
        dto.setRoleNames(null);
        Usuario entity = new Usuario();
        when(usuarioMapper.toEntity(dto)).thenReturn(entity);
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.saveFromDto(dto).getRoles()).isNull();
    }

    @Test
    @DisplayName("GIVEN Solo Gerente WHEN Puede Registrar Venta THEN Retorna True")
    void givenSoloGerente_whenPuedeRegistrarVenta_thenRetornaTrue() {
        Rol gerente = new Rol();
        gerente.setNombre(SecurityRoles.GERENTE);
        usuarioCompleto.setRoles(new HashSet<>(Set.of(gerente)));

        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isTrue();
    }

    @Test
    @DisplayName("GIVEN Password Prefijo2a WHEN Save THEN No Re Codifica")
    void givenPasswordPrefijo2a_whenSave_thenNoReCodifica() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("hash2a");
        nuevo.setPassword("$2a$existinghash");

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(nuevo).getPassword()).isEqualTo("$2a$existinghash");
    }

    @Test
    @DisplayName("GIVEN Password Prefijo2y WHEN Save THEN No Re Codifica")
    void givenPasswordPrefijo2y_whenSave_thenNoReCodifica() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("hash2y");
        nuevo.setPassword("$2y$existinghash");

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(nuevo).getPassword()).isEqualTo("$2y$existinghash");
    }

    @Test
    @DisplayName("GIVEN Sin Username WHEN Save THEN No Sanitiza Username")
    void givenSinUsername_whenSave_thenNoSanitizaUsername() {
        Usuario nuevo = new Usuario();
        nuevo.setNombre("SinUser");

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.save(nuevo);

        verify(inputSanitizer, org.mockito.Mockito.never()).sanitize(org.mockito.ArgumentMatchers.isNull());
    }

    @Test
    @DisplayName("GIVEN Lista Roles Vacia WHEN Save From Dto THEN No Asigna Roles")
    void givenListaRolesVacia_whenSaveFromDto_thenNoAsignaRoles() {
        com.minimarket.dto.usuario.UsuarioRequestDTO dto = new com.minimarket.dto.usuario.UsuarioRequestDTO();
        dto.setRoleNames(List.of());
        Usuario entity = new Usuario();
        when(usuarioMapper.toEntity(dto)).thenReturn(entity);
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.saveFromDto(dto).getRoles()).isNull();
    }

    @Test
    @DisplayName("GIVEN Password Blank WHEN Save THEN No Codifica")
    void givenPasswordBlank_whenSave_thenNoCodifica() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("blank");
        nuevo.setPassword("   ");

        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(nuevo).getPassword()).isEqualTo("   ");
    }

    @Test
    @DisplayName("GIVEN Rol No Permitido WHEN Puede Registrar Venta THEN Retorna False")
    void givenRolNoPermitido_whenPuedeRegistrarVenta_thenRetornaFalse() {
        Rol admin = new Rol();
        admin.setNombre("ADMIN");
        usuarioCompleto.setRoles(new HashSet<>(Set.of(admin)));

        assertThat(usuarioService.puedeRegistrarVenta(usuarioCompleto)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Existente Nuevo Password Plain WHEN Save THEN Codifica")
    void givenUsuarioExistenteNuevoPasswordPlain_whenSave_thenCodifica() {
        Usuario existing = new Usuario();
        existing.setId(1L);
        existing.setPassword("$2a$old");

        Usuario update = new Usuario();
        update.setId(1L);
        update.setUsername("cliente1");
        update.setPassword("nuevaClave");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("nuevaClave")).thenReturn("$2a$new");
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inputSanitizer.sanitize(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.save(update).getPassword()).isEqualTo("$2a$new");
    }
}
