package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.mapper.DevolucionMapper;
import com.tecsolutions.Veterinaria.mapper.NotaCreditoMapper;
import com.tecsolutions.Veterinaria.mapper.VentaMapper;
import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class VentaService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final DevolucionRepository devolucionRepository;
    private final ComprobanteSeqRepository comprobanteSeqRepository;
    private final ProductoService productoService;
    private final VentaMapper ventaMapper;
    private final DevolucionMapper devolucionMapper;
    private final NotaCreditoMapper notaCreditoMapper;

    public VentaService(
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            StockAlmacenRepository stockAlmacenRepository,
            NotaCreditoRepository notaCreditoRepository,
            DevolucionRepository devolucionRepository,
            ComprobanteSeqRepository comprobanteSeqRepository,
            ProductoService productoService,
            VentaMapper ventaMapper,
            DevolucionMapper devolucionMapper,
            NotaCreditoMapper notaCreditoMapper) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.notaCreditoRepository = notaCreditoRepository;
        this.devolucionRepository = devolucionRepository;
        this.comprobanteSeqRepository = comprobanteSeqRepository;
        this.productoService = productoService;
        this.ventaMapper = ventaMapper;
        this.devolucionMapper = devolucionMapper;
        this.notaCreditoMapper = notaCreditoMapper;
    }

    public VentaDTO crearVenta(VentaCreateDTO dto, String currentUsername, String currentRole) {
        Usuario currentUser = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Cliente cliente = null;
        if ("Cliente".equalsIgnoreCase(currentRole) || currentUser.getRol() == RolEnum.CLIENTE) {
            cliente = clienteRepository.findByUsuarioId(currentUser.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente no encontrado"));
            dto.setClienteId(cliente.getId());
        } else if (dto.getClienteId() != null) {
            cliente = clienteRepository.findById(dto.getClienteId()).orElse(null);
        }

        // Validate comprobante type
        String tipoComp = dto.getTipoComprobante() != null ? dto.getTipoComprobante().trim() : "Boleta";
        if (!"Boleta".equalsIgnoreCase(tipoComp) && !"Factura".equalsIgnoreCase(tipoComp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de comprobante inválido (Boleta o Factura)");
        }
        tipoComp = "Factura".equalsIgnoreCase(tipoComp) ? "Factura" : "Boleta";

        // Validate documento fiscal
        String doc = dto.getDocumentoFiscal() != null ? dto.getDocumentoFiscal().trim() : "";
        if ("Factura".equals(tipoComp)) {
            if (doc.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el RUC para emitir Factura");
            }
            if (!doc.matches("\\d{11}")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC debe tener 11 dígitos");
            }
        } else {
            if (doc.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el DNI o Carnet de Extranjería para emitir Boleta");
            }
            boolean esDni = doc.matches("\\d{8}");
            boolean esCe = doc.matches("20\\d{10}");
            if (!esDni && !esCe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Documento inválido: DNI (8 dígitos) o Carnet de Extranjería (12 dígitos, inicia con 20)");
            }
        }

        // Validate detalles list
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe incluir al menos un producto en la venta");
        }

        // Validate and load NotaCredito if supplied
        double montoNota = 0.0;
        NotaCredito notaCredito = null;
        if (dto.getNotaCreditoId() != null) {
            notaCredito = notaCreditoRepository.findById(dto.getNotaCreditoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nota de crédito no encontrada"));
            if (!"ACTIVA".equals(notaCredito.getEstado())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota de crédito no está activa");
            }
            if (notaCredito.getFechaVencimiento() != null && notaCredito.getFechaVencimiento().isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota de crédito está vencida");
            }
            if (dto.getClienteId() != null && notaCredito.getCliente() != null
                    && !notaCredito.getCliente().getId().equals(dto.getClienteId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota de crédito pertenece a otro cliente");
            }
            if (notaCredito.getSaldo() == null || notaCredito.getSaldo() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota de crédito no tiene saldo disponible");
            }
        }

        // Validate product stock, expiry, and warehouse state
        LocalDate hoy = LocalDate.now();
        double total = 0.0;

        for (DetalleVentaCreateDTO det : dto.getDetalles()) {
            if (det.getProductoId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto ID no especificado");
            }
            Producto prod = productoRepository.findById(det.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Stock insuficiente para el producto ID " + det.getProductoId()));

            int cantidad = det.getCantidad() != null ? det.getCantidad() : 0;
            if (cantidad <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
            }
            if (prod.getStock() == null || prod.getStock() < cantidad) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para el producto ID " + det.getProductoId());
            }
            if (prod.getFechaVencimiento() != null && prod.getFechaVencimiento().isBefore(hoy)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El producto '" + prod.getNombre() + "' está vencido y no puede venderse");
            }
            if (prod.getEstadoAlmacen() != null && prod.getEstadoAlmacen() != EstadoAlmacen.DISPONIBLE) {
                String estadoActual = prod.getEstadoAlmacen().getValue();
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El producto '" + prod.getNombre() + "' está en estado '" + estadoActual + "' y no puede venderse");
            }

            StockAlmacen stockDisp = productoService.getStockAlmacen(prod, "Disponible");
            int cantDisp = stockDisp.getCantidad() != null ? stockDisp.getCantidad() : 0;
            if (cantDisp < cantidad) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente en almacén 'Disponible' para el producto ID " + det.getProductoId() + " (disponible: " + cantDisp + ")");
            }

            prod.setStock(prod.getStock() - cantidad);
            stockDisp.setCantidad(cantDisp - cantidad);
            stockAlmacenRepository.save(stockDisp);
            productoRepository.save(prod);

            double pu = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : (prod.getPrecio() != null ? prod.getPrecio() : 0.0);
            total += cantidad * pu;
        }

        total = Math.round(total * 100.0) / 100.0;

        // Apply credit note discount
        if (notaCredito != null) {
            double saldo = notaCredito.getSaldo() != null ? notaCredito.getSaldo() : 0.0;
            montoNota = Math.min(total, saldo);
            total = Math.round((total - montoNota) * 100.0) / 100.0;
            double nuevoSaldo = Math.round((saldo - montoNota) * 100.0) / 100.0;
            notaCredito.setSaldo(nuevoSaldo);
            if (nuevoSaldo <= 0) {
                notaCredito.setEstado("CANJEADA");
            }
            notaCreditoRepository.save(notaCredito);
        }

        // Generate pickup code and correlative receipt
        String codigoRecojo = generarCodigoRecojo();
        String serie = "Factura".equals(tipoComp) ? "FA64" : "BA64";

        ComprobanteSeq seq = comprobanteSeqRepository.findBySerie(serie)
                .orElseGet(() -> {
                    int ultimo = 0;
                    List<Venta> ventasPorSerie = ventaRepository.findBySerie(serie);
                    for (Venta v : ventasPorSerie) {
                        if (v.getNumero() != null && v.getNumero().matches("\\d+")) {
                            try {
                                ultimo = Math.max(ultimo, Integer.parseInt(v.getNumero()));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    ComprobanteSeq nuevo = new ComprobanteSeq(null, serie, ultimo);
                    return comprobanteSeqRepository.saveAndFlush(nuevo);
                });

        seq.setUltimoNumero(seq.getUltimoNumero() + 1);
        comprobanteSeqRepository.save(seq);
        String numero = String.format("%010d", seq.getUltimoNumero());

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(currentUser);
        venta.setMetodoPago(dto.getMetodoPago());
        venta.setTipoComprobante(tipoComp);
        venta.setDocumentoFiscal(doc);
        venta.setSerie(serie);
        venta.setNumero(numero);
        venta.setTotal(total);
        venta.setMontoNota(montoNota);
        venta.setNotaCreditoId(notaCredito != null ? notaCredito.getId() : null);
        venta.setCodigoRecojo(codigoRecojo);
        venta.setEstado(VentaEstado.PENDIENTE);
        venta.setFecha(LocalDateTime.now());

        venta = ventaRepository.save(venta);

        for (DetalleVentaCreateDTO det : dto.getDetalles()) {
            Producto prod = productoRepository.findById(det.getProductoId()).orElseThrow();
            double pu = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : (prod.getPrecio() != null ? prod.getPrecio() : 0.0);
            double subtotal = det.getCantidad() * pu;

            DetalleVenta dv = new DetalleVenta();
            dv.setVenta(venta);
            dv.setProducto(prod);
            dv.setCantidad(det.getCantidad());
            dv.setPrecioUnitario(pu);
            dv.setSubtotal(subtotal);

            detalleVentaRepository.save(dv);
            venta.getDetalles().add(dv);

            productoService.registrarMovimiento(
                    prod,
                    "VENTA",
                    -det.getCantidad(),
                    null,
                    null,
                    "Venta " + venta.getTipoComprobante() + " " + (venta.getDocumentoFiscal() != null ? venta.getDocumentoFiscal() : ""),
                    "Venta #" + venta.getId(),
                    currentUser
            );
        }

        return ventaMapper.toDTO(venta);
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarVentas(int skip, int limit) {
        Pageable pageable = PageRequest.of(skip / Math.max(limit, 1), limit);
        return ventaRepository.findAllByOrderByFechaDesc(pageable).stream()
                .map(ventaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarMisVentas(String currentUsername) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un perfil de cliente asociado a este usuario"));

        return ventaRepository.findByClienteIdOrderByFechaDesc(cliente.getId()).stream()
                .map(ventaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerVenta(Long id, String currentUsername, String currentRole) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        if ("Cliente".equalsIgnoreCase(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (cliente == null || venta.getCliente() == null || !venta.getCliente().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta venta");
            }
        }

        return ventaMapper.toDTO(venta);
    }

    public VentaDTO solicitarAnulacion(Long id, String currentUsername) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);

        if (cliente == null || venta.getCliente() == null || !venta.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta venta");
        }

        if (venta.getEstado() != VentaEstado.PENDIENTE && venta.getEstado() != VentaEstado.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes solicitar anulación si la compra aún no ha sido entregada");
        }

        venta.setEstado(VentaEstado.ANULACION_SOLICITADA);
        venta = ventaRepository.save(venta);
        return ventaMapper.toDTO(venta);
    }

    public VentaDTO actualizarEstadoVenta(Long id, String nuevoEstadoStr, String currentUsername) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        Usuario currentUser = usuarioRepository.findByUsername(currentUsername).orElse(null);

        VentaEstado nuevoEstado;
        try {
            nuevoEstado = VentaEstado.fromValue(nuevoEstadoStr);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }

        VentaEstado actual = venta.getEstado();
        Map<VentaEstado, Set<VentaEstado>> transiciones = Map.of(
                VentaEstado.PENDIENTE, Set.of(VentaEstado.CONFIRMADA, VentaEstado.CANCELADA),
                VentaEstado.CONFIRMADA, Set.of(VentaEstado.ENTREGADA, VentaEstado.CANCELADA),
                VentaEstado.ENTREGADA, Set.of(),
                VentaEstado.ANULACION_SOLICITADA, Set.of(VentaEstado.CANCELADA, VentaEstado.CONFIRMADA)
        );

        Set<VentaEstado> permitidos = transiciones.getOrDefault(actual, Set.of());
        if (!permitidos.contains(nuevoEstado)) {
            String actualVal = actual != null ? actual.getValue() : "null";
            String nuevoVal = nuevoEstado != null ? nuevoEstado.getValue() : "null";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede pasar de '" + actualVal + "' a '" + nuevoVal + "'");
        }

        if (nuevoEstado == VentaEstado.CANCELADA) {
            revertirStockVenta(venta, currentUser, "ANULACION_VENTA");
        }

        venta.setEstado(nuevoEstado);
        venta = ventaRepository.save(venta);
        return ventaMapper.toDTO(venta);
    }

    public VentaDTO devolverVenta(Long id, DevolucionRequestDTO req, String currentUsername) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        Usuario currentUser = usuarioRepository.findByUsername(currentUsername).orElse(null);

        if (venta.getEstado() != VentaEstado.ENTREGADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden devolver compras ya entregadas");
        }

        if (devolucionRepository.existsByVentaId(venta.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta venta ya cuenta con una devolución registrada");
        }

        if (req.getTipo() == null || (!"EXTORNO".equalsIgnoreCase(req.getTipo()) && !"NOTA_CREDITO".equalsIgnoreCase(req.getTipo()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de devolución inválido (EXTORNO o NOTA_CREDITO)");
        }

        String tipoDevolucion = "NOTA_CREDITO".equalsIgnoreCase(req.getTipo()) ? "NOTA_CREDITO" : "EXTORNO";
        double monto = Math.round((req.getMonto() != null && req.getMonto() > 0 ? req.getMonto() : (venta.getTotal() != null ? venta.getTotal() : 0.0)) * 100.0) / 100.0;
        Long notaId = null;

        if ("NOTA_CREDITO".equals(tipoDevolucion)) {
            NotaCredito nota = new NotaCredito();
            nota.setCodigo(generarCodigoNota());
            nota.setCliente(venta.getCliente());
            nota.setVentaId(venta.getId());
            nota.setMonto(monto);
            nota.setSaldo(monto);
            nota.setFechaEmision(LocalDateTime.now());
            nota.setFechaVencimiento(LocalDateTime.now().plusDays(180));
            nota.setEstado("ACTIVA");
            nota.setMotivo(req.getMotivo());
            nota = notaCreditoRepository.save(nota);
            notaId = nota.getId();
        }

        Devolucion devolucion = new Devolucion();
        devolucion.setVenta(venta);
        devolucion.setCliente(venta.getCliente());
        devolucion.setTipo(tipoDevolucion);
        devolucion.setMonto(monto);
        devolucion.setMotivo(req.getMotivo());
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setUsuario(currentUser);
        devolucion.setNotaCreditoId(notaId);
        devolucionRepository.save(devolucion);

        revertirStockVenta(venta, currentUser, "DEVOLUCION");

        venta = ventaRepository.save(venta);
        return ventaMapper.toDTO(venta);
    }

    @Transactional(readOnly = true)
    public List<NotaCreditoDTO> listarNotasCredito(int skip, int limit, String currentUsername, String currentRole) {
        Pageable pageable = PageRequest.of(skip / Math.max(limit, 1), limit);
        if ("Cliente".equalsIgnoreCase(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
            if (usuario == null) {
                return List.of();
            }
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (cliente == null) {
                return List.of();
            }
            return notaCreditoRepository.findByClienteIdOrderByFechaEmisionDesc(cliente.getId(), pageable).stream()
                    .map(notaCreditoMapper::toDTO)
                    .toList();
        }

        return notaCreditoRepository.findAllByOrderByFechaEmisionDesc(pageable).stream()
                .map(notaCreditoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DevolucionDTO> listarDevoluciones(int skip, int limit) {
        Pageable pageable = PageRequest.of(skip / Math.max(limit, 1), limit);
        return devolucionRepository.findAllByOrderByFechaDesc(pageable).stream()
                .map(devolucionMapper::toDTO)
                .toList();
    }

    private void revertirStockVenta(Venta venta, Usuario usuario, String tipo) {
        for (DetalleVenta det : venta.getDetalles()) {
            if (det.getProducto() == null) {
                continue;
            }
            Producto prod = productoRepository.findById(det.getProducto().getId()).orElse(null);
            if (prod == null) {
                continue;
            }
            int cant = det.getCantidad() != null ? det.getCantidad() : 0;
            prod.setStock((prod.getStock() != null ? prod.getStock() : 0) + cant);
            productoService.sumarStockAlmacen(prod, "Disponible", cant);
            productoRepository.save(prod);

            productoService.registrarMovimiento(
                    prod,
                    tipo,
                    cant,
                    null,
                    null,
                    "Reverso de venta #" + venta.getId(),
                    "Venta #" + venta.getId(),
                    usuario
            );
        }
    }

    private String generarCodigoRecojo() {
        for (int i = 0; i < 50; i++) {
            StringBuilder sb = new StringBuilder("SRB-");
            for (int j = 0; j < 6; j++) {
                sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
            }
            String code = sb.toString();
            if (!ventaRepository.existsByCodigoRecojo(code)) {
                return code;
            }
        }
        return String.format("SRB-%06d", ventaRepository.count() + 1);
    }

    private String generarCodigoNota() {
        for (int i = 0; i < 50; i++) {
            StringBuilder sb = new StringBuilder("NC-");
            for (int j = 0; j < 6; j++) {
                sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
            }
            String code = sb.toString();
            if (!notaCreditoRepository.existsByCodigo(code)) {
                return code;
            }
        }
        return String.format("NC-%06d", notaCreditoRepository.count() + 1);
    }
}
