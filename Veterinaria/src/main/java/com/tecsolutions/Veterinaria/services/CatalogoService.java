package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.CategoriaDTO;
import com.tecsolutions.Veterinaria.dto.FamiliaDTO;
import com.tecsolutions.Veterinaria.dto.MarcaDTO;
import com.tecsolutions.Veterinaria.dto.ProveedorDTO;
import com.tecsolutions.Veterinaria.mapper.CategoriaMapper;
import com.tecsolutions.Veterinaria.mapper.FamiliaMapper;
import com.tecsolutions.Veterinaria.mapper.MarcaMapper;
import com.tecsolutions.Veterinaria.mapper.ProveedorMapper;
import com.tecsolutions.Veterinaria.model.Categoria;
import com.tecsolutions.Veterinaria.model.Familia;
import com.tecsolutions.Veterinaria.model.Marca;
import com.tecsolutions.Veterinaria.model.Proveedor;
import com.tecsolutions.Veterinaria.repository.CategoriaRepository;
import com.tecsolutions.Veterinaria.repository.FamiliaRepository;
import com.tecsolutions.Veterinaria.repository.MarcaRepository;
import com.tecsolutions.Veterinaria.repository.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CatalogoService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    private final FamiliaRepository familiaRepository;
    private final FamiliaMapper familiaMapper;

    private final MarcaRepository marcaRepository;
    private final MarcaMapper marcaMapper;

    public CatalogoService(
            ProveedorRepository proveedorRepository,
            ProveedorMapper proveedorMapper,
            CategoriaRepository categoriaRepository,
            CategoriaMapper categoriaMapper,
            FamiliaRepository familiaRepository,
            FamiliaMapper familiaMapper,
            MarcaRepository marcaRepository,
            MarcaMapper marcaMapper) {
        this.proveedorRepository = proveedorRepository;
        this.proveedorMapper = proveedorMapper;
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
        this.familiaRepository = familiaRepository;
        this.familiaMapper = familiaMapper;
        this.marcaRepository = marcaRepository;
        this.marcaMapper = marcaMapper;
    }

    // --- Proveedores ---

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listarProveedores(int skip, int limit) {
        return proveedorRepository.findAll().stream()
                .skip(skip)
                .limit(limit)
                .map(proveedorMapper::toDTO)
                .toList();
    }

    public ProveedorDTO crearProveedor(ProveedorDTO dto) {
        if (dto.getRuc() != null && !dto.getRuc().isBlank()) {
            proveedorRepository.findByRuc(dto.getRuc()).ifPresent(p -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUC ya está registrado");
            });
        }
        Proveedor proveedor = proveedorMapper.toEntity(dto);
        proveedor.setId(null);
        proveedor = proveedorRepository.save(proveedor);
        return proveedorMapper.toDTO(proveedor);
    }

    public ProveedorDTO actualizarProveedor(Long id, ProveedorDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        if (dto.getRuc() != null && !dto.getRuc().isBlank()) {
            proveedorRepository.findByRuc(dto.getRuc()).ifPresent(p -> {
                if (!p.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUC ya está registrado");
                }
            });
        }

        proveedor.setRuc(dto.getRuc());
        proveedor.setRazonSocial(dto.getRazonSocial());
        proveedor.setContacto(dto.getContacto());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setEmail(dto.getEmail());
        if (dto.getMoneda() != null) {
            proveedor.setMoneda(dto.getMoneda());
        }

        proveedor = proveedorRepository.save(proveedor);
        return proveedorMapper.toDTO(proveedor);
    }

    public Map<String, String> eliminarProveedor(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        proveedorRepository.delete(proveedor);
        return Map.of("detail", "Proveedor eliminado");
    }

    // --- Categorías ---

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(categoriaMapper::toDTO)
                .toList();
    }

    public CategoriaDTO crearCategoria(CategoriaDTO dto) {
        categoriaRepository.findByNombre(dto.getNombre()).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoría ya registrada");
        });
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoria);
    }

    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        categoriaRepository.findByNombreAndIdNot(dto.getNombre(), id).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoría ya registrada");
        });

        categoria.setNombre(dto.getNombre());
        categoria = categoriaRepository.save(categoria);
        return categoriaMapper.toDTO(categoria);
    }

    public Map<String, String> eliminarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        categoriaRepository.delete(categoria);
        return Map.of("detail", "Categoría eliminada");
    }

    // --- Familias ---

    @Transactional(readOnly = true)
    public List<FamiliaDTO> listarFamilias() {
        return familiaRepository.findAllByOrderByNombreAsc().stream()
                .map(familiaMapper::toDTO)
                .toList();
    }

    public FamiliaDTO crearFamilia(FamiliaDTO dto) {
        familiaRepository.findByNombre(dto.getNombre()).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Familia ya registrada");
        });
        Familia familia = new Familia();
        familia.setNombre(dto.getNombre());
        familia = familiaRepository.save(familia);
        return familiaMapper.toDTO(familia);
    }

    public FamiliaDTO actualizarFamilia(Long id, FamiliaDTO dto) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Familia no encontrada"));

        familiaRepository.findByNombreAndIdNot(dto.getNombre(), id).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Familia ya registrada");
        });

        familia.setNombre(dto.getNombre());
        familia = familiaRepository.save(familia);
        return familiaMapper.toDTO(familia);
    }

    public Map<String, String> eliminarFamilia(Long id) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Familia no encontrada"));
        familiaRepository.delete(familia);
        return Map.of("detail", "Familia eliminada");
    }

    // --- Marcas ---

    @Transactional(readOnly = true)
    public List<MarcaDTO> listarMarcas() {
        return marcaRepository.findAllByOrderByNombreAsc().stream()
                .map(marcaMapper::toDTO)
                .toList();
    }

    public MarcaDTO crearMarca(MarcaDTO dto) {
        marcaRepository.findByNombre(dto.getNombre()).ifPresent(m -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Marca ya registrada");
        });
        Marca marca = new Marca();
        marca.setNombre(dto.getNombre());
        marca = marcaRepository.save(marca);
        return marcaMapper.toDTO(marca);
    }

    public MarcaDTO actualizarMarca(Long id, MarcaDTO dto) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));

        marcaRepository.findByNombreAndIdNot(dto.getNombre(), id).ifPresent(m -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Marca ya registrada");
        });

        marca.setNombre(dto.getNombre());
        marca = marcaRepository.save(marca);
        return marcaMapper.toDTO(marca);
    }

    public Map<String, String> eliminarMarca(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
        marcaRepository.delete(marca);
        return Map.of("detail", "Marca eliminada");
    }
}
