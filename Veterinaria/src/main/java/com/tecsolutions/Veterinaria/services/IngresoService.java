package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.IngresoDTO;
import com.tecsolutions.Veterinaria.dto.IngresoDetalleDTO;
import com.tecsolutions.Veterinaria.mapper.IngresoMapper;
import com.tecsolutions.Veterinaria.model.Ingreso;
import com.tecsolutions.Veterinaria.model.IngresoDetalle;
import com.tecsolutions.Veterinaria.model.Producto;
import com.tecsolutions.Veterinaria.model.Proveedor;
import com.tecsolutions.Veterinaria.model.StockAlmacen;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.IngresoDetalleRepository;
import com.tecsolutions.Veterinaria.repository.IngresoRepository;
import com.tecsolutions.Veterinaria.repository.ProductoRepository;
import com.tecsolutions.Veterinaria.repository.ProveedorRepository;
import com.tecsolutions.Veterinaria.repository.StockAlmacenRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final IngresoDetalleRepository ingresoDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final ProductoService productoService;
    private final IngresoMapper ingresoMapper;

    public IngresoService(
            IngresoRepository ingresoRepository,
            IngresoDetalleRepository ingresoDetalleRepository,
            ProveedorRepository proveedorRepository,
            ProductoRepository productoRepository,
            StockAlmacenRepository stockAlmacenRepository,
            ProductoService productoService,
            IngresoMapper ingresoMapper) {
        this.ingresoRepository = ingresoRepository;
        this.ingresoDetalleRepository = ingresoDetalleRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.productoService = productoService;
        this.ingresoMapper = ingresoMapper;
    }

    @Transactional(readOnly = true)
    public List<IngresoDTO> listarIngresos(int skip, int limit) {
        Pageable pageable = PageRequest.of(skip / Math.max(limit, 1), limit);
        return ingresoRepository.findAllByOrderByFechaDesc(pageable).stream()
                .map(ingresoMapper::toDTO)
                .toList();
    }

    public IngresoDTO crearIngreso(IngresoDTO dto, Usuario usuario) {
        if (dto.getProveedorId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado");
        }
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe incluir al menos un detalle de ingreso");
        }

        double total = 0.0;
        List<Producto> productosModificados = new ArrayList<>();

        for (IngresoDetalleDTO det : dto.getDetalles()) {
            if (det.getProductoId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto ID no especificado");
            }
            Producto prod = productoRepository.findById(det.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto ID " + det.getProductoId() + " no encontrado"));

            if (det.getCantidad() == null || det.getCantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
            }

            if (det.getFechaVencimiento() != null && det.getFechaVencimiento().isBefore(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La fecha de vencimiento de " + prod.getNombre() + " no puede ser anterior a hoy");
            }

            prod.setStock((prod.getStock() != null ? prod.getStock() : 0) + det.getCantidad());
            productoService.sumarStockAlmacen(prod, "Disponible", det.getCantidad());

            if (det.getPrecioUnitario() != null) {
                prod.setPrecioCompra(det.getPrecioUnitario());
            }
            if (det.getPrecioVenta() != null && det.getPrecioVenta() > 0) {
                prod.setPrecio(det.getPrecioVenta());
            }
            if (det.getFechaVencimiento() != null) {
                prod.setEsPerecible(true);
                if (prod.getFechaVencimiento() == null || det.getFechaVencimiento().isBefore(prod.getFechaVencimiento())) {
                    prod.setFechaVencimiento(det.getFechaVencimiento());
                }
            }

            prod = productoRepository.save(prod);
            productosModificados.add(prod);

            double pu = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : 0.0;
            total += det.getCantidad() * pu;
        }

        String numeroFactura = dto.getNumeroFactura();
        if ((numeroFactura == null || numeroFactura.isBlank()) && dto.getSerie() != null && dto.getNumero() != null) {
            numeroFactura = dto.getSerie() + "-" + dto.getNumero();
        }

        Ingreso ingreso = new Ingreso();
        ingreso.setProveedor(proveedor);
        ingreso.setTipoPago(dto.getTipoPago() != null ? dto.getTipoPago() : "Contado");
        ingreso.setTipoComprobante(dto.getTipoComprobante() != null ? dto.getTipoComprobante() : "Factura");
        ingreso.setSerie(dto.getSerie());
        ingreso.setNumero(dto.getNumero());
        ingreso.setNumeroFactura(numeroFactura);
        ingreso.setTotal(total);
        ingreso.setUsuario(usuario);
        ingreso.setFecha(LocalDateTime.now());

        ingreso = ingresoRepository.save(ingreso);

        for (int i = 0; i < dto.getDetalles().size(); i++) {
            IngresoDetalleDTO det = dto.getDetalles().get(i);
            Producto prod = productosModificados.get(i);
            double pu = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : 0.0;
            double subtotal = det.getCantidad() * pu;

            IngresoDetalle detalle = new IngresoDetalle();
            detalle.setIngreso(ingreso);
            detalle.setProducto(prod);
            detalle.setCantidad(det.getCantidad());
            detalle.setPrecioUnitario(det.getPrecioUnitario());
            detalle.setPrecioVenta(det.getPrecioVenta());
            detalle.setFechaVencimiento(det.getFechaVencimiento());
            detalle.setSubtotal(subtotal);

            ingresoDetalleRepository.save(detalle);
            ingreso.getDetalles().add(detalle);

            productoService.registrarMovimiento(
                    prod,
                    "INGRESO",
                    det.getCantidad(),
                    null,
                    null,
                    "Ingreso de mercadería de " + proveedor.getRazonSocial(),
                    "Ingreso #" + ingreso.getId(),
                    usuario
            );
        }

        return ingresoMapper.toDTO(ingreso);
    }

    public Map<String, String> eliminarIngreso(Long id, Usuario usuario) {
        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingreso no encontrado"));

        for (IngresoDetalle det : ingreso.getDetalles()) {
            Producto prod = det.getProducto();
            if (prod != null) {
                int cant = det.getCantidad() != null ? det.getCantidad() : 0;
                prod.setStock(Math.max(0, (prod.getStock() != null ? prod.getStock() : 0) - cant));

                StockAlmacen rowDisp = productoService.getStockAlmacen(prod, "Disponible");
                rowDisp.setCantidad(Math.max(0, (rowDisp.getCantidad() != null ? rowDisp.getCantidad() : 0) - cant));
                stockAlmacenRepository.save(rowDisp);
                productoRepository.save(prod);

                productoService.registrarMovimiento(
                        prod,
                        "ANULACION_INGRESO",
                        -cant,
                        null,
                        null,
                        "Anulación de ingreso #" + ingreso.getId(),
                        "Ingreso #" + ingreso.getId(),
                        usuario
                );
            }
        }

        ingresoRepository.delete(ingreso);
        return Map.of("detail", "Ingreso eliminado y stock revertido");
    }
}
