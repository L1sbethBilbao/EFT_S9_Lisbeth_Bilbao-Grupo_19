package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.Rol;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataInitializerTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private StockSucursalRepository stockSucursalRepository;

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    private Rol rolCliente;
    private Rol rolEmpleado;
    private Rol rolGerente;

    @BeforeEach
    void setUp() {
        rolCliente = rol(SecurityRoles.CLIENTE);
        rolEmpleado = rol(SecurityRoles.EMPLEADO);
        rolGerente = rol(SecurityRoles.GERENTE);
    }

    @Test
    @DisplayName("GIVEN Con Base Vacia WHEN Run THEN Crea Roles Usuarios Y Semilla Catalogo")
    void givenConBaseVacia_whenRun_thenCreaRolesUsuariosYSemillaCatalogo() {
        stubGuardadoRepositorios();
        stubRolesNuevos();
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.count()).thenReturn(0L);
        when(sucursalRepository.count()).thenReturn(0L);
        Producto productoDemo = new Producto();
        productoDemo.setNombre("Arroz grano largo 1kg");
        productoDemo.setPrecio(1890.0);
        productoDemo.setStock(30);
        when(productoRepository.findAll()).thenReturn(List.of(productoDemo));

        dataInitializer.run();

        verify(rolRepository, times(3)).save(any(Rol.class));
        verify(categoriaRepository, times(5)).save(any(Categoria.class));
        verify(productoRepository, times(7)).save(any(Producto.class));
        verify(inventarioRepository, times(7)).save(any(Inventario.class));
        verify(sucursalRepository, times(3)).save(any(Sucursal.class));
        verify(stockSucursalRepository, times(3)).save(any(StockSucursal.class));
        verify(promocionRepository, times(2)).save(any(Promocion.class));
    }

    @Test
    @DisplayName("GIVEN Si Ya Hay Categorias WHEN Run THEN No Semilla Catalogo")
    void givenSiYaHayCategorias_whenRun_thenNoSemillaCatalogo() {
        stubGuardadoRepositorios();
        stubRolesNuevos();
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(categoriaRepository.count()).thenReturn(3L);
        when(sucursalRepository.count()).thenReturn(1L);

        dataInitializer.run();

        verify(categoriaRepository, never()).save(any(Categoria.class));
        verify(productoRepository, never()).save(any(Producto.class));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("GIVEN Si Roles Ya Existen WHEN Run THEN No Los Recrea")
    void givenSiRolesYaExisten_whenRun_thenNoLosRecrea() {
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE)).thenReturn(Optional.of(rolCliente));
        when(rolRepository.findByNombre(SecurityRoles.EMPLEADO)).thenReturn(Optional.of(rolEmpleado));
        when(rolRepository.findByNombre(SecurityRoles.GERENTE)).thenReturn(Optional.of(rolGerente));
        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(categoriaRepository.count()).thenReturn(1L);
        when(sucursalRepository.count()).thenReturn(1L);

        dataInitializer.run();

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    @DisplayName("GIVEN Usuario Existente Con Perfil Completo WHEN Run THEN No Sobrescribe Datos")
    void givenUsuarioExistenteConPerfilCompleto_whenRun_thenNoSobrescribeDatos() {
        stubRolesNuevos();
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario clienteExistente = usuarioConPerfilCompleto("cliente1");
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(clienteExistente));
        when(usuarioRepository.findByUsername("empleado1")).thenReturn(Optional.empty());
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(categoriaRepository.count()).thenReturn(1L);
        when(sucursalRepository.count()).thenReturn(1L);

        dataInitializer.run();

        assertThat(clienteExistente.getNombre()).isEqualTo("Juan");
        assertThat(clienteExistente.getApellido()).isEqualTo("Perez");
        assertThat(clienteExistente.getEmail()).isEqualTo("juan@test.cl");
        assertThat(clienteExistente.getDireccion()).isEqualTo("Calle 1");
        verify(usuarioRepository).save(clienteExistente);
    }

    @Test
    @DisplayName("GIVEN Usuario Existente Con Campos Blank WHEN Run THEN Completa Perfil")
    void givenUsuarioExistenteConCamposBlank_whenRun_thenCompletaPerfil() {
        stubRolesNuevos();
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario clienteExistente = new Usuario();
        clienteExistente.setUsername("cliente1");
        clienteExistente.setNombre("   ");
        clienteExistente.setApellido("");
        clienteExistente.setEmail(" ");
        clienteExistente.setDireccion("  ");

        when(usuarioRepository.findByUsername("cliente1")).thenReturn(Optional.of(clienteExistente));
        when(usuarioRepository.findByUsername("empleado1")).thenReturn(Optional.empty());
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(categoriaRepository.count()).thenReturn(1L);
        when(sucursalRepository.count()).thenReturn(1L);

        dataInitializer.run();

        assertThat(clienteExistente.getNombre()).isEqualTo("Cliente");
        assertThat(clienteExistente.getApellido()).isEqualTo("Demo");
        assertThat(clienteExistente.getEmail()).isEqualTo("cliente1@minimarket.cl");
        assertThat(clienteExistente.getDireccion()).isEqualTo("Av. Principal 100, Santiago");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Run THEN Lanza Excepcion Si Rol No Existe Al Crear Usuario")
    void givenDefaultContext_whenRun_thenLanzaExcepcionSiRolNoExisteAlCrearUsuario() {
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE))
                .thenReturn(Optional.empty(), Optional.empty());
        when(rolRepository.findByNombre(SecurityRoles.EMPLEADO)).thenReturn(Optional.empty());
        when(rolRepository.findByNombre(SecurityRoles.GERENTE)).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> dataInitializer.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Rol no encontrado: " + SecurityRoles.CLIENTE);
    }

    @Test
    @DisplayName("GIVEN Valor Null WHEN Capitalizar THEN Retorna Usuario")
    void givenValorNull_whenCapitalizar_thenRetornaUsuario() throws Exception {
        assertThat(invocarCapitalizar(null)).isEqualTo("Usuario");
    }

    @Test
    @DisplayName("GIVEN Valor Vacio WHEN Capitalizar THEN Retorna Usuario")
    void givenValorVacio_whenCapitalizar_thenRetornaUsuario() throws Exception {
        assertThat(invocarCapitalizar("")).isEqualTo("Usuario");
    }

    @Test
    @DisplayName("GIVEN Valor Valido WHEN Capitalizar THEN Capitaliza Primera Letra")
    void givenValorValido_whenCapitalizar_thenCapitalizaPrimeraLetra() throws Exception {
        assertThat(invocarCapitalizar("cliente")).isEqualTo("Cliente");
    }

    private void stubGuardadoRepositorios() {
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    }

    private void stubRolesNuevos() {
        when(rolRepository.findByNombre(SecurityRoles.CLIENTE))
                .thenReturn(Optional.empty(), Optional.of(rolCliente));
        when(rolRepository.findByNombre(SecurityRoles.EMPLEADO))
                .thenReturn(Optional.empty(), Optional.of(rolEmpleado));
        when(rolRepository.findByNombre(SecurityRoles.GERENTE))
                .thenReturn(Optional.empty(), Optional.of(rolGerente));
    }

    private static Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }

    private static Usuario usuarioConPerfilCompleto(String username) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@test.cl");
        usuario.setDireccion("Calle 1");
        return usuario;
    }

    private String invocarCapitalizar(String value) throws Exception {
        Method method = DataInitializer.class.getDeclaredMethod("capitalizar", String.class);
        method.setAccessible(true);
        return (String) method.invoke(dataInitializer, value);
    }
}
