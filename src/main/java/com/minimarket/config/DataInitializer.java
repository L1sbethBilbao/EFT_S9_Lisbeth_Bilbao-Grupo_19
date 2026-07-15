package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            InventarioRepository inventarioRepository,
            PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearRolSiNoExiste(SecurityRoles.CLIENTE);
        crearRolSiNoExiste(SecurityRoles.EMPLEADO);
        crearRolSiNoExiste(SecurityRoles.GERENTE);

        crearUsuarioDemo("cliente1", "cliente123", SecurityRoles.CLIENTE);
        crearUsuarioDemo("empleado1", "empleado123", SecurityRoles.EMPLEADO);
        crearUsuarioDemo("gerente1", "gerente123", SecurityRoles.GERENTE);

        if (categoriaRepository.count() == 0) {
            seedCatalogoMinimarket();
        }
    }

    private void seedCatalogoMinimarket() {
        Categoria abarrotes = crearCategoria("Abarrotes");
        Categoria bebidas = crearCategoria("Bebidas");
        Categoria lacteos = crearCategoria("Lácteos y congelados");
        Categoria limpieza = crearCategoria("Artículos de limpieza");
        Categoria cuidado = crearCategoria("Cuidado personal");

        Producto arroz = crearProducto("Arroz grano largo 1kg", 1890.0, 120,
                "Arroz de consumo diario, ideal para familias.", abarrotes);
        Producto atun = crearProducto("Atún en lata 160g", 1290.0, 80,
                "Conserva de atún en agua.", abarrotes);
        Producto coca = crearProducto("Bebida cola 1.5L", 1990.0, 60,
                "Refresco para consumo inmediato.", bebidas);
        Producto agua = crearProducto("Agua mineral 1.5L", 990.0, 100,
                "Agua mineral sin gas.", bebidas);
        Producto leche = crearProducto("Leche entera 1L", 1090.0, 45,
                "Producto refrigerado. Requiere cadena de frío.", lacteos);
        Producto detergente = crearProducto("Detergente líquido 1L", 3490.0, 35,
                "Limpieza del hogar.", limpieza);
        Producto shampoo = crearProducto("Shampoo anticaspa 400ml", 2990.0, 40,
                "Cuidado personal diario.", cuidado);

        registrarEntradaInventario(arroz, 120);
        registrarEntradaInventario(atun, 80);
        registrarEntradaInventario(coca, 60);
        registrarEntradaInventario(agua, 100);
        registrarEntradaInventario(leche, 45);
        registrarEntradaInventario(detergente, 35);
        registrarEntradaInventario(shampoo, 40);
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    private Producto crearProducto(String nombre, Double precio, Integer stock, String descripcion,
                                   Categoria categoria) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    private void registrarEntradaInventario(Producto producto, int cantidad) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());
        inventarioRepository.save(inventario);
    }

    private void crearRolSiNoExiste(String nombre) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rolRepository.save(rol);
        }
    }

    private void crearUsuarioDemo(String username, String password, String rolNombre) {
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + rolNombre));

        usuarioRepository.findByUsername(username).ifPresentOrElse(
                existing -> completarPerfilDemo(existing, username),
                () -> {
                    Usuario usuario = new Usuario();
                    usuario.setUsername(username);
                    usuario.setPassword(passwordEncoder.encode(password));
                    usuario.setRoles(new HashSet<>(Set.of(rol)));
                    usuario.setRetentionExcluded(true);
                    completarPerfilDemo(usuario, username);
                    usuarioRepository.save(usuario);
                });
    }

    private void completarPerfilDemo(Usuario usuario, String username) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            usuario.setNombre(capitalizar(username.replaceAll("\\d", "")));
        }
        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            usuario.setApellido("Demo");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            usuario.setEmail(username + "@minimarket.cl");
        }
        if (usuario.getDireccion() == null || usuario.getDireccion().isBlank()) {
            usuario.setDireccion("Av. Principal 100, Santiago");
        }
        usuarioRepository.save(usuario);
    }

    private String capitalizar(String value) {
        if (value == null || value.isEmpty()) {
            return "Usuario";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
