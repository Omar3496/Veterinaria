package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ProductoDTO;
import com.tecsolutions.Veterinaria.dto.StockAlmacenItemDTO;
import com.tecsolutions.Veterinaria.model.Producto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductoMapper {

    public ProductoDTO toDTO(Producto entity) {
        if (entity == null) {
            return null;
        }

        List<StockAlmacenItemDTO> stockPorAlmacen = new ArrayList<>();
        if (entity.getStocksAlmacen() != null) {
            stockPorAlmacen = entity.getStocksAlmacen().stream()
                    .filter(s -> s.getCantidad() != null && s.getCantidad() > 0)
                    .map(s -> new StockAlmacenItemDTO(s.getAlmacen(), s.getCantidad()))
                    .toList();
        }

        String estadoAlmacenStr = entity.getEstadoAlmacen() != null ? entity.getEstadoAlmacen().getValue() : "Disponible";

        return ProductoDTO.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .codigoBarra(entity.getCodigoBarra())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .precio(entity.getPrecio())
                .stock(entity.getStock() != null ? entity.getStock() : 0)
                .stockMinimo(entity.getStockMinimo() != null ? entity.getStockMinimo() : 5)
                .fechaVencimiento(entity.getFechaVencimiento())
                .esPerecible(Boolean.TRUE.equals(entity.getEsPerecible()))
                .precioCompra(entity.getPrecioCompra() != null ? entity.getPrecioCompra() : 0.0)
                .estadoAlmacen(estadoAlmacenStr)
                .proveedorId(entity.getProveedor() != null ? entity.getProveedor().getId() : null)
                .categoriaId(entity.getCategoria() != null ? entity.getCategoria().getId() : null)
                .familiaId(entity.getFamilia() != null ? entity.getFamilia().getId() : null)
                .marcaId(entity.getMarca() != null ? entity.getMarca().getId() : null)
                .imagen(entity.getImagen())
                .proveedorNombre(entity.getProveedorNombre())
                .categoriaNombre(entity.getCategoriaNombre())
                .familiaNombre(entity.getFamiliaNombre())
                .marcaNombre(entity.getMarcaNombre())
                .estadoAlmacenNombre(entity.getEstadoAlmacenNombre())
                .stockPorAlmacen(stockPorAlmacen)
                .build();
    }
}
