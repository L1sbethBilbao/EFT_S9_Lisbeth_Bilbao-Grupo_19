package com.minimarket.service.impl;

import com.minimarket.constants.BusinessErrorMessages;
import com.minimarket.constants.PedidoConstants;
import com.minimarket.dto.pedido.PedidoItemDTO;
import com.minimarket.dto.pedido.PedidoRegistroDTO;
import com.minimarket.entity.DetallePedido;
import com.minimarket.entity.Pedido;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.PedidoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.PedidoService;
import com.minimarket.service.PromocionService;
import com.minimarket.service.StockSucursalService;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            PedidoConstants.ESTADO_PENDIENTE,
            PedidoConstants.ESTADO_CONFIRMADO,
            PedidoConstants.ESTADO_EN_CAMINO,
            PedidoConstants.ESTADO_ENTREGADO,
            PedidoConstants.ESTADO_CANCELADO
    );

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private StockSucursalService stockSucursalService;

    @Autowired
    private PromocionService promocionService;

    @Override
    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    @Override
    public Page<Pedido> findAll(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }

    @Override
    public Pedido findById(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Override
    public Pedido save(Pedido pedido) {
        if (pedido.getTotal() == null) {
            pedido.setTotal(0.0);
        }
        return pedidoRepository.save(pedido);
    }

    @Override
    public List<Pedido> findByUsuarioId(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public Pedido registrarPedido(PedidoRegistroDTO dto) {
        Usuario usuario = usuarioService.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.USUARIO_NO_ENCONTRADO, dto.getUsuarioId())));

        usuarioService.validarDatosCompletos(usuario);

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.SUCURSAL_NO_ENCONTRADA, dto.getSucursalId())));

        validarTipoEntrega(dto);

        List<DetallePedido> detalles = new ArrayList<>();
        for (PedidoItemDTO item : dto.getItems()) {
            stockSucursalService.validarStockDisponible(
                    dto.getSucursalId(), item.getProductoId(), item.getCantidad());

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(BusinessErrorMessages.PRODUCTO_NO_ENCONTRADO, item.getProductoId())));

            double precioUnitario = promocionService.aplicarDescuento(
                    item.getProductoId(), dto.getSucursalId(), producto.getPrecio());

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(precioUnitario * item.getCantidad());
            detalles.add(detalle);

            stockSucursalService.decrementarStock(
                    dto.getSucursalId(), item.getProductoId(), item.getCantidad());
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setSucursal(sucursal);
        pedido.setFecha(new Date());
        pedido.setEstado(PedidoConstants.ESTADO_PENDIENTE);
        pedido.setTipoEntrega(dto.getTipoEntrega().toUpperCase());
        pedido.setDireccionEntrega(dto.getDireccionEntrega());
        pedido.setTotal(calcularTotal(detalles));
        pedido.setDetalles(detalles);
        detalles.forEach(d -> d.setPedido(pedido));

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido actualizarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format(BusinessErrorMessages.PEDIDO_NO_ENCONTRADO, id)));

        String estadoNormalizado = nuevoEstado.toUpperCase();
        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            throw new IllegalArgumentException(
                    String.format(BusinessErrorMessages.ESTADO_PEDIDO_INVALIDO, nuevoEstado));
        }

        pedido.setEstado(estadoNormalizado);
        return pedidoRepository.save(pedido);
    }

    @Override
    public double calcularTotal(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0.0;
        }
        return detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
    }

    private void validarTipoEntrega(PedidoRegistroDTO dto) {
        String tipo = dto.getTipoEntrega().toUpperCase();
        if (!PedidoConstants.TIPO_RETIRO.equals(tipo) && !PedidoConstants.TIPO_DESPACHO.equals(tipo)) {
            throw new IllegalArgumentException(
                    String.format(BusinessErrorMessages.TIPO_ENTREGA_INVALIDO, dto.getTipoEntrega()));
        }
        if (PedidoConstants.TIPO_DESPACHO.equals(tipo)
                && (dto.getDireccionEntrega() == null || dto.getDireccionEntrega().isBlank())) {
            throw new IllegalArgumentException(BusinessErrorMessages.DIRECCION_ENTREGA_OBLIGATORIA);
        }
    }
}
