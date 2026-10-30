package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.CambioEstadoAlmacenRequestDTO;
import com.tecsolutions.Veterinaria.dto.MovimientoAlmacenRequestDTO;
import com.tecsolutions.Veterinaria.dto.NextSkuResponseDTO;
import com.tecsolutions.Veterinaria.dto.ProductoDTO;
import com.tecsolutions.Veterinaria.mapper.ProductoMapper;
import com.tecsolutions.Veterinaria.model.EstadoAlmacen;
import com.tecsolutions.Veterinaria.model.MovimientoStock;
import com.tecsolutions.Veterinaria.model.Producto;
import com.tecsolutions.Veterinaria.model.StockAlmacen;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.CategoriaRepository;
import com.tecsolutions.Veterinaria.repository.FamiliaRepository;
import com.tecsolutions.Veterinaria.repository.MarcaRepository;
import com.tecsolutions.Veterinaria.repository.MovimientoStockRepository;
import com.tecsolutions.Veterinaria.repository.ProductoRepository;
import com.tecsolutions.Veterinaria.repository.ProveedorRepository;
import com.tecsolutions.Veterinaria.repository.StockAlmacenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProductoService {

    private static final long SKU_INICIAL = 20260001L;

    private final ProductoRepository productoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final FamiliaRepository familiaRepository;
    private final MarcaRepository marcaRepository;
    private final ProductoMapper productoMapper;

    public ProductoService(
            ProductoRepository productoRepository,
            StockAlmacenRepository stockAlmacenRepository,
            MovimientoStockRepository movimientoStockRepository,
            ProveedorRepository proveedorRepository,
            CategoriaRepository categoriaRepository,
            FamiliaRepository familiaRepository,
            MarcaRepository marcaRepository,
            ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.proveedorRepository = proveedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.familiaRepository = familiaRepository;
        this.marcaRepository = marcaRepository;
        this.productoMapper = productoMapper;
    }

    @Transactional(readOnly = true)
    public NextSkuResponseDTO obtenerSiguienteSku() {
        List<Producto> productos = productoRepository.findAll();
        long maxSku = SKU_INICIAL - 1;
        for (Producto p : productos) {
            String s = p.getSku();
            if (s != null && s.matches("\\d+")) {
                try {
                    long val = Long.parseLong(s);
                    if (val > maxSku) {
                        maxSku = val;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return new NextSkuResponseDTO(String.valueOf(maxSku + 1));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarProductos(int skip, int limit) {
        return productoRepository.findAll().stream()
                .skip(skip)
                .limit(limit)
                .map(p -> {
                    asegurarStockAlmacen(p);
                    return productoMapper.toDTO(p);
                })
                .toList();
    }

    public ProductoDTO crearProducto(ProductoDTO dto) {
        String sku = dto.getSku();
        if (sku == null || sku.isBlank()) {
            sku = obtenerSiguienteSku().getSku();
        }

        if (productoRepository.existsBySku(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El SKU ya está en uso");
        }

        EstadoAlmacen estadoAlmacen = EstadoAlmacen.DISPONIBLE;
        if (dto.getEstadoAlmacen() != null && !dto.getEstadoAlmacen().isBlank()) {
            try {
                estadoAlmacen = EstadoAlmacen.fromValue(dto.getEstadoAlmacen());
            } catch (Exception e) {
                estadoAlmacen = EstadoAlmacen.DISPONIBLE;
            }
        }

        Producto producto = new Producto();
        producto.setSku(sku);
        producto.setCodigoBarra(dto.getCodigoBarra());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock() != null ? dto.getStock() : 0);
        producto.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
        producto.setFechaVencimiento(dto.getFechaVencimiento());
        producto.setEsPerecible(Boolean.TRUE.equals(dto.getEsPerecible()));
        producto.setPrecioCompra(dto.getPrecioCompra() != null ? dto.getPrecioCompra() : 0.0);
        producto.setEstadoAlmacen(estadoAlmacen);
        producto.setImagen(dto.getImagen());

        if (dto.getProveedorId() != null) {
            proveedorRepository.findById(dto.getProveedorId()).ifPresent(producto::setProveedor);
        }
        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId()).ifPresent(producto::setCategoria);
        }
        if (dto.getFamiliaId() != null) {
            familiaRepository.findById(dto.getFamiliaId()).ifPresent(producto::setFamilia);
        }
        if (dto.getMarcaId() != null) {
            marcaRepository.findById(dto.getMarcaId()).ifPresent(producto::setMarca);
        }

        producto = productoRepository.save(producto);

        // Asegurar desglose por almacén inicial
        String nombreAlmacen = estadoAlmacen != null ? estadoAlmacen.getValue() : "Disponible";
        StockAlmacen stockAlmacen = new StockAlmacen(null, producto, nombreAlmacen, producto.getStock() != null ? producto.getStock() : 0);
        stockAlmacen = stockAlmacenRepository.save(stockAlmacen);
        producto.getStocksAlmacen().add(stockAlmacen);

        return productoMapper.toDTO(producto);
    }

    public ProductoDTO actualizarProducto(Long id, ProductoDTO dto, Usuario usuario) {
        Producto dbProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (dto.getSku() != null && !dto.getSku().isBlank() && !dto.getSku().equals(dbProducto.getSku())) {
            if (productoRepository.existsBySku(dto.getSku())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El SKU ya está en uso");
            }
            dbProducto.setSku(dto.getSku());
        }

        int stockNuevo = dto.getStock() != null ? dto.getStock() : 0;
        int stockActual = dbProducto.getStock() != null ? dbProducto.getStock() : 0;
        int diferenciaStock = stockNuevo - stockActual;

        dbProducto.setCodigoBarra(dto.getCodigoBarra());
        dbProducto.setNombre(dto.getNombre());
        dbProducto.setDescripcion(dto.getDescripcion());
        dbProducto.setPrecio(dto.getPrecio());
        dbProducto.setStock(stockNuevo);
        dbProducto.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5);
        dbProducto.setFechaVencimiento(dto.getFechaVencimiento());
        dbProducto.setEsPerecible(Boolean.TRUE.equals(dto.getEsPerecible()));
        dbProducto.setPrecioCompra(dto.getPrecioCompra() != null ? dto.getPrecioCompra() : 0.0);
        dbProducto.setImagen(dto.getImagen());

        if (dto.getEstadoAlmacen() != null && !dto.getEstadoAlmacen().isBlank()) {
            try {
                dbProducto.setEstadoAlmacen(EstadoAlmacen.fromValue(dto.getEstadoAlmacen()));
            } catch (Exception ignored) {
            }
        }

        if (dto.getProveedorId() != null) {
            proveedorRepository.findById(dto.getProveedorId()).ifPresent(dbProducto::setProveedor);
        }
        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId()).ifPresent(dbProducto::setCategoria);
        }
        if (dto.getFamiliaId() != null) {
            familiaRepository.findById(dto.getFamiliaId()).ifPresent(dbProducto::setFamilia);
        }
        if (dto.getMarcaId() != null) {
            marcaRepository.findById(dto.getMarcaId()).ifPresent(dbProducto::setMarca);
        }

        if (diferenciaStock != 0) {
            if (diferenciaStock > 0) {
                sumarStockAlmacen(dbProducto, "Disponible", diferenciaStock);
            } else {
                restarStockAlmacen(dbProducto, "Disponible", Math.abs(diferenciaStock));
            }
            registrarMovimiento(
                    dbProducto,
                    "AJUSTE",
                    diferenciaStock,
                    null,
                    null,
                    "Ajuste de stock desde edición de producto",
                    null,
                    usuario
            );
        }

        dbProducto = productoRepository.save(dbProducto);
        return productoMapper.toDTO(dbProducto);
    }

    public ProductoDTO actualizarStock(Long id, int cantidad, String operacion, Usuario usuario) {
        Producto dbProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        int stockActual = dbProducto.getStock() != null ? dbProducto.getStock() : 0;
        boolean esResta = operacion != null && operacion.equalsIgnoreCase("restar");

        if (esResta) {
            if (stockActual < cantidad) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
            }
            dbProducto.setStock(stockActual - cantidad);
            restarStockAlmacen(dbProducto, "Disponible", cantidad);
        } else {
            dbProducto.setStock(stockActual + cantidad);
            sumarStockAlmacen(dbProducto, "Disponible", cantidad);
        }

        registrarMovimiento(
                dbProducto,
                "AJUSTE",
                esResta ? -cantidad : cantidad,
                null,
                null,
                "Ajuste de stock (" + (esResta ? "restar" : "sumar") + ")",
                null,
                usuario
        );

        dbProducto = productoRepository.save(dbProducto);
        return productoMapper.toDTO(dbProducto);
    }

    public ProductoDTO cambiarEstadoAlmacen(Long id, String nuevoEstado, String observacion, Usuario usuario) {
        Producto dbProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        EstadoAlmacen targetEstado;
        try {
            targetEstado = EstadoAlmacen.fromValue(nuevoEstado);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de almacén inválido");
        }

        String estadoAnterior = dbProducto.getEstadoAlmacen() != null ? dbProducto.getEstadoAlmacen().getValue() : "Disponible";
        if (estadoAnterior.equals(targetEstado.getValue())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto ya se encuentra en ese estado");
        }

        asegurarStockAlmacen(dbProducto);
        StockAlmacen origen = getStockAlmacen(dbProducto, estadoAnterior);
        StockAlmacen destino = getStockAlmacen(dbProducto, targetEstado.getValue());

        int cantidad = origen.getCantidad() != null ? origen.getCantidad() : 0;
        origen.setCantidad(0);
        destino.setCantidad((destino.getCantidad() != null ? destino.getCantidad() : 0) + cantidad);

        stockAlmacenRepository.save(origen);
        stockAlmacenRepository.save(destino);

        dbProducto.setEstadoAlmacen(targetEstado);
        dbProducto = productoRepository.save(dbProducto);

        registrarMovimiento(
                dbProducto,
                "CAMBIO_ALMACEN",
                cantidad,
                estadoAnterior,
                targetEstado.getValue(),
                observacion,
                null,
                usuario
        );

        return productoMapper.toDTO(dbProducto);
    }

    public ProductoDTO moverStock(MovimientoAlmacenRequestDTO req, Usuario usuario) {
        Producto dbProducto = productoRepository.findById(req.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (req.getCantidad() == null || req.getCantidad() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");
        }

        if (req.getAlmacenOrigen() == null || req.getAlmacenDestino() == null
                || req.getAlmacenOrigen().equalsIgnoreCase(req.getAlmacenDestino())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El almacén de origen y destino deben ser distintos");
        }

        EstadoAlmacen origenEstado;
        try {
            origenEstado = EstadoAlmacen.fromValue(req.getAlmacenOrigen());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Almacén de origen inválido");
        }

        EstadoAlmacen destinoEstado;
        try {
            destinoEstado = EstadoAlmacen.fromValue(req.getAlmacenDestino());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Almacén de destino inválido");
        }

        String origenNombre = origenEstado.getValue();
        String destinoNombre = destinoEstado.getValue();

        StockAlmacen origen = getStockAlmacen(dbProducto, origenNombre);
        int cantOrigen = origen.getCantidad() != null ? origen.getCantidad() : 0;
        if (cantOrigen < req.getCantidad()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente en '" + origenNombre + "' (disponible: " + cantOrigen + ")");
        }

        StockAlmacen destino = getStockAlmacen(dbProducto, destinoNombre);
        origen.setCantidad(cantOrigen - req.getCantidad());
        destino.setCantidad((destino.getCantidad() != null ? destino.getCantidad() : 0) + req.getCantidad());

        stockAlmacenRepository.save(origen);
        stockAlmacenRepository.save(destino);

        String estadoActual = dbProducto.getEstadoAlmacen() != null ? dbProducto.getEstadoAlmacen().getValue() : "Disponible";
        if (estadoActual.equals(origenNombre) && origen.getCantidad() == 0) {
            dbProducto.setEstadoAlmacen(destinoEstado);
            dbProducto = productoRepository.save(dbProducto);
        }

        registrarMovimiento(
                dbProducto,
                "CAMBIO_ALMACEN",
                req.getCantidad(),
                origenNombre,
                destinoNombre,
                req.getObservacion(),
                null,
                usuario
        );

        return productoMapper.toDTO(dbProducto);
    }

    public Map<String, String> eliminarProducto(Long id) {
        Producto dbProducto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        productoRepository.delete(dbProducto);
        return Map.of("detail", "Producto eliminado");
    }

    // --- Helpers de StockAlmacen y MovimientoStock ---

    public boolean asegurarStockAlmacen(Producto producto) {
        if (stockAlmacenRepository.existsByProductoId(producto.getId())) {
            return false;
        }
        String almacen = producto.getEstadoAlmacen() != null ? producto.getEstadoAlmacen().getValue() : "Disponible";
        StockAlmacen row = new StockAlmacen(null, producto, almacen, producto.getStock() != null ? producto.getStock() : 0);
        stockAlmacenRepository.save(row);
        producto.getStocksAlmacen().add(row);
        return true;
    }

    public StockAlmacen getStockAlmacen(Producto producto, String almacen) {
        asegurarStockAlmacen(producto);
        return stockAlmacenRepository.findByProductoIdAndAlmacen(producto.getId(), almacen)
                .orElseGet(() -> {
                    StockAlmacen row = new StockAlmacen(null, producto, almacen, 0);
                    row = stockAlmacenRepository.save(row);
                    producto.getStocksAlmacen().add(row);
                    return row;
                });
    }

    public StockAlmacen sumarStockAlmacen(Producto producto, String almacen, int cantidad) {
        StockAlmacen row = getStockAlmacen(producto, almacen);
        row.setCantidad((row.getCantidad() != null ? row.getCantidad() : 0) + cantidad);
        return stockAlmacenRepository.save(row);
    }

    public StockAlmacen restarStockAlmacen(Producto producto, String almacen, int cantidad) {
        StockAlmacen row = getStockAlmacen(producto, almacen);
        int actual = row.getCantidad() != null ? row.getCantidad() : 0;
        if (actual < cantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stock insuficiente en almacén '" + almacen + "' (disponible: " + actual + ")");
        }
        row.setCantidad(actual - cantidad);
        return stockAlmacenRepository.save(row);
    }

    public void registrarMovimiento(
            Producto producto,
            String tipo,
            int cantidad,
            String estadoAnterior,
            String estadoNuevo,
            String observacion,
            String referencia,
            Usuario usuario) {
        MovimientoStock mov = new MovimientoStock();
        mov.setProducto(producto);
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setEstadoAnterior(estadoAnterior);
        mov.setEstadoNuevo(estadoNuevo);
        mov.setObservacion(observacion);
        mov.setReferencia(referencia);
        mov.setUsuario(usuario);
        mov.setFecha(LocalDateTime.now());
        movimientoStockRepository.save(mov);
    }
}
