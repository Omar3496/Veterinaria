package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.RecordatorioVacunaDTO;
import com.tecsolutions.Veterinaria.dto.VacunaCreateDTO;
import com.tecsolutions.Veterinaria.dto.VacunaDTO;
import com.tecsolutions.Veterinaria.mapper.VacunaMapper;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Producto;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.model.Vacuna;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.ProductoRepository;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
import com.tecsolutions.Veterinaria.repository.VacunaRepository;
import static com.tecsolutions.Veterinaria.security.SecurityUtils.isClienteRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class VacunaService {

    private final VacunaRepository vacunaRepository;
    private final MascotaRepository mascotaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoService productoService;
    private final VacunaMapper vacunaMapper;

    public VacunaService(
            VacunaRepository vacunaRepository,
            MascotaRepository mascotaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            ProductoService productoService,
            VacunaMapper vacunaMapper) {
        this.vacunaRepository = vacunaRepository;
        this.mascotaRepository = mascotaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoService = productoService;
        this.vacunaMapper = vacunaMapper;
    }

    public VacunaDTO crearVacuna(Long mascotaId, VacunaCreateDTO dto, String currentUsername) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        Usuario vet = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String tipo = (dto.getTipo() != null) ? dto.getTipo().trim() : "";
        Producto producto = null;

        if (dto.getProductoId() != null) {
            producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
            if (producto.getStock() == null || producto.getStock() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para '" + producto.getNombre() + "'");
            }
            if (tipo.isEmpty()) {
                tipo = producto.getNombre();
            }
        }

        if (tipo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes indicar el tipo de vacuna o elegir un producto del inventario");
        }

        LocalDate fechaAplicacion = dto.getFechaAplicacion() != null ? dto.getFechaAplicacion() : LocalDate.now();
        LocalDate proximaDosis = dto.getProximaDosis();
        if (dto.getIntervaloDias() != null && dto.getIntervaloDias() > 0) {
            proximaDosis = fechaAplicacion.plusDays(dto.getIntervaloDias());
        }

        if (producto != null) {
            producto.setStock(producto.getStock() - 1);
            productoRepository.save(producto);
            productoService.restarStockAlmacen(producto, "Disponible", 1);
        }

        Vacuna dbVacuna = new Vacuna();
        dbVacuna.setTipo(tipo);
        dbVacuna.setFechaAplicacion(fechaAplicacion);
        dbVacuna.setProximaDosis(proximaDosis);
        dbVacuna.setProducto(producto);
        dbVacuna.setIntervaloDias(dto.getIntervaloDias());
        dbVacuna.setMascota(mascota);
        dbVacuna.setVeterinario(vet);

        dbVacuna = vacunaRepository.save(dbVacuna);
        return vacunaMapper.toDTO(dbVacuna);
    }

    @Transactional(readOnly = true)
    public List<RecordatorioVacunaDTO> obtenerRecordatorios(String currentUsername, String currentRole) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(15);

        List<Vacuna> vacunas;
        if (isClienteRole(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            vacunas = vacunaRepository.findByMascotaPropietarioUsuarioIdAndProximaDosisNotNullAndProximaDosisLessThanEqual(usuario.getId(), limite);
        } else {
            vacunas = vacunaRepository.findByProximaDosisNotNullAndProximaDosisLessThanEqual(limite);
        }

        List<Vacuna> ordenadas = new ArrayList<>(vacunas);
        ordenadas.sort(Comparator.comparing(Vacuna::getProximaDosis));

        List<RecordatorioVacunaDTO> resultados = new ArrayList<>();
        for (Vacuna v : ordenadas) {
            String mascotaNombre = (v.getMascota() != null) ? v.getMascota().getNombre() : null;
            String clienteNombre = (v.getMascota() != null && v.getMascota().getPropietario() != null) ? v.getMascota().getPropietario().getNombre() : null;
            String clienteTelefono = (v.getMascota() != null && v.getMascota().getPropietario() != null) ? v.getMascota().getPropietario().getTelefono() : null;

            resultados.add(RecordatorioVacunaDTO.builder()
                    .vacunaId(v.getId())
                    .mascotaId(v.getMascota() != null ? v.getMascota().getId() : null)
                    .tipo(v.getTipo())
                    .proximaDosis(v.getProximaDosis())
                    .mascotaNombre(mascotaNombre)
                    .clienteNombre(clienteNombre)
                    .clienteTelefono(clienteTelefono)
                    .build());
        }
        return resultados;
    }
}
