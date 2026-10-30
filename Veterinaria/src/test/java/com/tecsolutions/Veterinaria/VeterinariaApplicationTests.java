package com.tecsolutions.Veterinaria;

import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class VeterinariaApplicationTests {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private NotaCreditoRepository notaCreditoRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Test
    void contextLoadsAndModelsPersistWithSpanishEnums() {
        // 1. Dual enum parsing (constant name + Spanish label)
        assertEquals(RolEnum.ADMIN, RolEnum.fromValue("ADMIN"));
        assertEquals(RolEnum.ADMIN, RolEnum.fromValue("Administrador"));
        assertEquals(EstadoCita.PROGRAMADA, EstadoCita.fromValue("PROGRAMADA"));
        assertEquals(EstadoCita.PROGRAMADA, EstadoCita.fromValue("Programada"));
        assertEquals(EstadoAlmacen.NO_DISPONIBLE, EstadoAlmacen.fromValue("NO_DISPONIBLE"));
        assertEquals(EstadoAlmacen.NO_DISPONIBLE, EstadoAlmacen.fromValue("No disponible"));
        assertEquals(EstadoAlmacen.DANIADO, EstadoAlmacen.fromValue("Dañado"));
        assertEquals(VentaEstado.ANULACION_SOLICITADA, VentaEstado.fromValue("ANULACION_SOLICITADA"));
        assertEquals(VentaEstado.ANULACION_SOLICITADA, VentaEstado.fromValue("Anulación solicitada"));

        // 2. Persist Usuario -> Cliente -> Mascota -> Cita
        Usuario vet = new Usuario(null, "vet_test", "hash", RolEnum.VETERINARIO, true, "vet@test.pe");
        vet = usuarioRepository.saveAndFlush(vet);

        Cliente cliente = new Cliente();
        cliente.setDni("12345678");
        cliente.setNombre("Juan Perez");
        cliente.setTelefono("999888777");
        cliente = clienteRepository.saveAndFlush(cliente);

        Mascota mascota = new Mascota();
        mascota.setNombre("Firulais");
        mascota.setEspecie("Perro");
        mascota.setFechaNacimiento(LocalDate.of(2022, 1, 15));
        mascota.setPropietario(cliente);
        mascota = mascotaRepository.saveAndFlush(mascota);

        Cita cita = new Cita();
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setMotivo("Control");
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita = citaRepository.saveAndFlush(cita);
        assertEquals("vet_test", cita.getVeterinarioNombre());

        // 3. Persist Producto, Venta & NotaCredito computed fields
        Producto prod = new Producto();
        prod.setSku("20260001");
        prod.setNombre("Vacuna Antirrábica");
        prod.setPrecio(50.0);
        prod.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
        prod = productoRepository.saveAndFlush(prod);
        assertEquals("Disponible", prod.getEstadoAlmacenNombre());

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(vet);
        venta.setMetodoPago("EFECTIVO");
        venta.setSerie("BA64");
        venta.setNumero("0000000001");
        venta.setEstado(VentaEstado.CONFIRMADA);
        venta = ventaRepository.saveAndFlush(venta);
        assertEquals("BA64-0000000001", venta.getComprobante());
        assertEquals("Confirmada", venta.getEstadoNombre());

        NotaCredito nc = new NotaCredito();
        nc.setCodigo("NC-ABCDEF");
        nc.setCliente(cliente);
        nc.setMonto(50.0);
        nc.setSaldo(25.0);
        nc.setEstado("ACTIVA");
        nc.setFechaVencimiento(LocalDateTime.now().plusDays(180));
        nc = notaCreditoRepository.saveAndFlush(nc);
        assertTrue(nc.isVigente());

        // 4. Configuracion string PK
        Configuracion cfg = configuracionRepository.saveAndFlush(new Configuracion("horarios", "{\"duracion_min\":30}"));
        assertEquals("horarios", cfg.getClave());
    }
}
