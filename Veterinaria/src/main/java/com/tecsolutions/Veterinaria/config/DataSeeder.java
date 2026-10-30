package com.tecsolutions.Veterinaria.config;

import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final FamiliaRepository familiaRepository;
    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;
    private final StockAlmacenRepository stockAlmacenRepository;
    private final CitaRepository citaRepository;
    private final ExamenAuxiliarRepository examenAuxiliarRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            MascotaRepository mascotaRepository,
            ProveedorRepository proveedorRepository,
            CategoriaRepository categoriaRepository,
            FamiliaRepository familiaRepository,
            MarcaRepository marcaRepository,
            ProductoRepository productoRepository,
            StockAlmacenRepository stockAlmacenRepository,
            CitaRepository citaRepository,
            ExamenAuxiliarRepository examenAuxiliarRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.proveedorRepository = proveedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.familiaRepository = familiaRepository;
        this.marcaRepository = marcaRepository;
        this.productoRepository = productoRepository;
        this.stockAlmacenRepository = stockAlmacenRepository;
        this.citaRepository = citaRepository;
        this.examenAuxiliarRepository = examenAuxiliarRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed();
    }

    @Transactional
    public synchronized void seed() {
        if (usuarioRepository.count() > 0) {
            return;
        }

        // 1. Usuarios demo
        Usuario admin = new Usuario(null, "admin", passwordEncoder.encode("admin123"), RolEnum.ADMIN, true, "admin@sirbull.pe");
        Usuario vet = new Usuario(null, "vet", passwordEncoder.encode("vet123"), RolEnum.VETERINARIO, true, "vet@sirbull.pe");
        Usuario recep = new Usuario(null, "recep", passwordEncoder.encode("recep123"), RolEnum.RECEPCIONISTA, true, "recep@sirbull.pe");
        Usuario jperez = new Usuario(null, "jperez", passwordEncoder.encode("cliente123"), RolEnum.CLIENTE, true, "juan@example.com");
        Usuario mlopez = new Usuario(null, "mlopez", passwordEncoder.encode("cliente123"), RolEnum.CLIENTE, true, "maria@example.com");

        admin = usuarioRepository.save(admin);
        vet = usuarioRepository.save(vet);
        recep = usuarioRepository.save(recep);
        jperez = usuarioRepository.save(jperez);
        mlopez = usuarioRepository.save(mlopez);

        // 2. Clientes y Mascotas
        Cliente cliente1 = new Cliente();
        cliente1.setDni("12345678");
        cliente1.setNombre("Juan Perez");
        cliente1.setTelefono("999888777");
        cliente1.setEmail("juan@example.com");
        cliente1.setUsuario(jperez);
        cliente1 = clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setDni("87654321");
        cliente2.setNombre("Maria Lopez");
        cliente2.setTelefono("999111222");
        cliente2.setEmail("maria@example.com");
        cliente2.setUsuario(mlopez);
        cliente2 = clienteRepository.save(cliente2);

        Mascota mascota1 = new Mascota();
        mascota1.setNombre("Firulais");
        mascota1.setEspecie("Perro");
        mascota1.setRaza("Labrador");
        mascota1.setFechaNacimiento(LocalDate.of(2020, 5, 10));
        mascota1.setPropietario(cliente1);

        Mascota mascota2 = new Mascota();
        mascota2.setNombre("Michi");
        mascota2.setEspecie("Gato");
        mascota2.setRaza("Siames");
        mascota2.setPropietario(cliente1);

        Mascota mascota3 = new Mascota();
        mascota3.setNombre("Rex");
        mascota3.setEspecie("Perro");
        mascota3.setRaza("Pastor Aleman");
        mascota3.setPropietario(cliente2);

        mascota1 = mascotaRepository.save(mascota1);
        mascota2 = mascotaRepository.save(mascota2);
        mascota3 = mascotaRepository.save(mascota3);

        // 3. Proveedor
        Proveedor prov1 = new Proveedor();
        prov1.setRuc("20101010101");
        prov1.setRazonSocial("Distribuidora Vet SAC");
        prov1.setContacto("Carlos Vargas");
        prov1.setTelefono("01234567");
        prov1.setMoneda("SOL");
        prov1 = proveedorRepository.save(prov1);

        // 4. Citas de ejemplo
        Cita cita1 = new Cita();
        cita1.setFechaHora(LocalDateTime.now().plusDays(1));
        cita1.setMotivo("Vacunacion Anual");
        cita1.setTipo("Consulta médica");
        cita1.setEstado(EstadoCita.PROGRAMADA);
        cita1.setMascota(mascota1);
        cita1.setVeterinario(vet);
        citaRepository.save(cita1);

        Cita cita2 = new Cita();
        cita2.setFechaHora(LocalDateTime.now());
        cita2.setMotivo("Consulta General");
        cita2.setTipo("Consulta médica");
        cita2.setEstado(EstadoCita.COMPLETADA);
        cita2.setMascota(mascota2);
        citaRepository.save(cita2);

        // 5. Catálogo de Exámenes Auxiliares
        seedExamenes();

        // 6. Catálogo de Productos (P001, P002 + P003..P047)
        seedProductos(prov1);
    }

    private void seedExamenes() {
        record ExamenDef(String nombre, String descripcion, Double precio, String tiempoEstimado) {}
        List<ExamenDef> catalogo = List.of(
                new ExamenDef("Hemograma completo", "Conteo de células sanguíneas para evaluar infecciones o anemia.", 45.0, "15 min"),
                new ExamenDef("Perfil bioquímico (función hepática y renal)", "Evaluación de órganos como hígado y riñones.", 80.0, "20 min"),
                new ExamenDef("Radiografía (Rayos X)", "Imágenes diagnósticas de tórax, abdomen o extremidades.", 90.0, "15 min"),
                new ExamenDef("Ecografía abdominal", "Estudio ecográfico de órganos internos.", 120.0, "25 min"),
                new ExamenDef("Examen coprológico", "Detección de parásitos intestinales en heces.", 35.0, "10 min"),
                new ExamenDef("Prueba de sarna (raspado cutáneo)", "Detección de ácaros en la piel.", 40.0, "10 min"),
                new ExamenDef("Test de leucemia e inmunodeficiencia felina", "Detección de enfermedades virales en gatos.", 70.0, "15 min"),
                new ExamenDef("Electrocardiograma (ECG)", "Evaluación de la actividad eléctrica del corazón.", 60.0, "15 min")
        );

        for (ExamenDef def : catalogo) {
            if (examenAuxiliarRepository.findByNombre(def.nombre()).isEmpty()) {
                ExamenAuxiliar exam = new ExamenAuxiliar(null, def.nombre(), def.descripcion(), def.precio(), def.tiempoEstimado(), true);
                examenAuxiliarRepository.save(exam);
            }
        }
    }

    private void seedProductos(Proveedor proveedor) {
        record ClasifRule(String categoria, List<String> keywords) {}
        List<ClasifRule> clasificaciones = List.of(
                new ClasifRule("Alimento para Perros", List.of("dogchow", "dog", "croqueta", "pedigree", "raza", "labrador", "comida seca", "humeda", "sobres")),
                new ClasifRule("Alimento para Gatos", List.of("gato", "whiskas", "cat")),
                new ClasifRule("Golosinas y Snacks", List.of("snack", "premio", "bocadito", "galleta")),
                new ClasifRule("Higiene y Aseo", List.of("champu", "jabon", "shampoo", "cepillo", "peine")),
                new ClasifRule("Farmacia y Vacunas", List.of("vacuna", "antibiotico", "analgesico", "jeringa", "antipulgas", "garrapatas", "desparasitante", "amoxicilina")),
                new ClasifRule("Juguetes", List.of("pelota", "hueso", "cuerda", "mordedor", "juguete")),
                new ClasifRule("Accesorios", List.of("correa", "collar", "arnes", "cama", "manta", "colchon"))
        );

        List<String> marcasLista = List.of(
                "DogChow", "Purina", "Pro Plan", "Royal Canin", "Hill's", "Pedigree", "Whiskas", "Optimum", "NexGard", "Bravecto", "Advantage", "Kong"
        );

        record ProdDef(String sku, String nombre, Double precio, Integer stock, Integer stockMinimo, String descripcion) {}
        List<ProdDef> productos = List.of(
                // P001 & P002
                new ProdDef("P001", "Vacuna Antirrabica", 50.0, 20, 5, "Vacuna antirrabica para perros y gatos."),
                new ProdDef("P002", "Comida DogChow 15kg", 120.0, 10, 3, "Comida para perros adultos DogChow 15kg."),
                // P003..P047
                new ProdDef("P003", "Purina Pro Plan Adulto Raza Mediana 10kg", 145.0, 15, 3, "Croquetas premium para perros adultos de raza mediana."),
                new ProdDef("P004", "Royal Canin Labrador Adulto 12kg", 210.0, 12, 3, "Comida seca especializada para Labrador Adulto."),
                new ProdDef("P005", "Pedigree Comida Seca Adulto Carne 15kg", 132.5, 18, 4, "Croquetas con carne para perros adultos."),
                new ProdDef("P006", "Hill's Science Diet Adulto 10kg", 198.0, 8, 2, "Nutrición científica para perros adultos."),
                new ProdDef("P007", "Optimum Adulto Raza Pequeña 8kg", 112.0, 20, 4, "Croquetas para perros adultos de razas pequeñas."),
                new ProdDef("P008", "Pedigree Comida Húmeda Carne en Lata 400g", 8.5, 40, 10, "Lata húmeda con carne para perros."),
                new ProdDef("P009", "Pedigree Sobres Húmedos Pollo y Verduras 12u", 34.0, 30, 8, "Sobres húmedos de pollo para perros."),
                new ProdDef("P010", "Purina Pro Plan Comida Húmeda Cachorro 6 latas", 52.0, 16, 4, "Alimento húmedo para cachorros en latas."),
                new ProdDef("P011", "Whiskas Adulto Pescado 1.5kg", 48.0, 25, 5, "Comida seca para gatos adultos, sabor pescado."),
                new ProdDef("P012", "Whiskas Sobres Pollo 12u", 30.0, 28, 6, "Sobres húmedos de pollo para gatos."),
                new ProdDef("P013", "Royal Canin Gato Adulto 2kg", 92.0, 14, 3, "Comida seca para gatos adultos."),
                new ProdDef("P014", "Purina Cat Chow Adulto 3kg", 55.0, 22, 5, "Croquetas balanceadas para gatos adultos."),
                new ProdDef("P015", "Bocaditos de Res Premium 500g", 22.0, 35, 8, "Bocaditos de res para premiar a tu perro."),
                new ProdDef("P016", "Galletas Caninas Sabor Hueso 400g", 18.5, 30, 6, "Galletas crujientes sabor hueso."),
                new ProdDef("P017", "Premio Dental Fresh 30u", 27.0, 26, 6, "Premios que ayudan a la limpieza dental."),
                new ProdDef("P018", "Snack de Pollo Deshidratado 200g", 24.5, 18, 4, "Snack natural de pollo deshidratado."),
                new ProdDef("P019", "Champú Antipulgas y Garrapatas 500ml", 38.0, 20, 5, "Champú que ayuda a eliminar pulgas y garrapatas."),
                new ProdDef("P020", "Jabón Neutro para Mascotas 250g", 15.0, 24, 5, "Jabón suave de pH neutro."),
                new ProdDef("P021", "Shampoo Seco Avena y Miel 300ml", 32.0, 17, 4, "Shampoo sin agua con avena y miel."),
                new ProdDef("P022", "Champú Acondicionador para Gatos 350ml", 35.0, 15, 4, "Champú suave especial para gatos."),
                new ProdDef("P023", "Cepillo Cepilladora de Goma", 21.0, 30, 6, "Cepilladora de goma para retirar pelo muerto."),
                new ProdDef("P024", "Peine Anti Pulgas Acero Inoxidable", 19.5, 28, 6, "Peine de acero para eliminar pulgas y enredos."),
                new ProdDef("P025", "Cepillo Cepillador para Cachorro", 17.0, 22, 5, "Cepillo suave ideal para cachorros."),
                new ProdDef("P026", "Vacuna Triple Felina", 55.0, 12, 3, "Vacuna triple felina (RCP)."),
                new ProdDef("P027", "Vacuna Polivalente Canina", 60.0, 14, 3, "Vacuna polivalente canina."),
                new ProdDef("P028", "Antibiótico Amoxicilina 100ml", 42.0, 10, 3, "Suspensión antibiótica de amoxicilina."),
                new ProdDef("P029", "Jeringa Descartable 5ml x10", 12.5, 50, 10, "Jeringas descartables de 5 ml."),
                new ProdDef("P030", "Carprofeno Analgesico 25mg x10", 48.0, 12, 3, "Analgésico y antiinflamatorio canino."),
                new ProdDef("P031", "NexGard Tableta Antipulgas 10kg", 85.0, 16, 4, "Tableta mensual contra pulgas y garrapatas."),
                new ProdDef("P032", "Bravecto Antipulgas y Garrapatas 20kg", 120.0, 9, 2, "Protección de 3 meses contra pulgas y garrapatas."),
                new ProdDef("P033", "Desparasitante Interno Amplio Espectro 6 tab", 32.0, 20, 5, "Desparasitante interno de amplio espectro."),
                new ProdDef("P034", "Advantage Antipulgas Spot-On 4 pipetas", 75.0, 13, 3, "Pipetas antipulgas para gatos y perros."),
                new ProdDef("P035", "Pelota Kong Clásica Roja", 29.0, 25, 5, "Pelota resistente y saltarina de caucho."),
                new ProdDef("P036", "Pelota con Sonaja 2u", 16.5, 32, 6, "Pelotas con sonaja para jugar."),
                new ProdDef("P037", "Pelota Tennis Reforzada para Perros", 14.0, 36, 6, "Pelotas tipo tennis resistentes a mordidas."),
                new ProdDef("P038", "Hueso de Goma Masticable Mediano", 23.0, 27, 5, "Hueso de goma resistente para masticar."),
                new ProdDef("P039", "Cuerda Trenzada de Algodón 2 nudos", 20.0, 30, 6, "Cuerda de algodón trenzada con nudos."),
                new ProdDef("P040", "Mordedor Dental con Sabor a Pollo", 19.0, 24, 5, "Mordedor con sabor que ayuda a la higiene dental."),
                new ProdDef("P041", "Juguete Squeaky Pollito de Peluche", 22.5, 18, 4, "Juguete de peluche que emite sonido."),
                new ProdDef("P042", "Correa Retráctil 5m Nylon", 45.0, 20, 5, "Correa retráctil de 5 metros."),
                new ProdDef("P043", "Collar Ajustable Nylon Reflectante", 16.0, 34, 6, "Collar de nylon ajustable y reflectante."),
                new ProdDef("P044", "Arnés Pechera Perro Mediano", 38.0, 15, 4, "Arnés de pechera cómodo para paseos."),
                new ProdDef("P045", "Cama Ortopédica Mediana Antideslizante", 89.0, 10, 3, "Cama ortopédica con base antideslizante."),
                new ProdDef("P046", "Manta de Felpa Suave 90x60", 34.0, 20, 5, "Manta de felpa para mascotas."),
                new ProdDef("P047", "Colchón Suave para Perro y Gato", 65.0, 12, 3, "Colchón mullido de espuma.")
        );

        Map<String, Categoria> categoriasCache = new HashMap<>();
        Map<String, Familia> familiasCache = new HashMap<>();
        Map<String, Marca> marcasCache = new HashMap<>();

        for (ProdDef p : productos) {
            if (productoRepository.findBySku(p.sku()).isPresent()) {
                continue;
            }

            String n = p.nombre().toLowerCase();

            String catNombre = "Otros";
            for (ClasifRule rule : clasificaciones) {
                if (rule.keywords().stream().anyMatch(n::contains)) {
                    catNombre = rule.categoria();
                    break;
                }
            }

            String finalCatNombre = catNombre;
            Categoria cat = categoriasCache.computeIfAbsent(catNombre, name ->
                    categoriaRepository.findByNombre(name).orElseGet(() ->
                            categoriaRepository.save(new Categoria(null, finalCatNombre, new ArrayList<>()))
                    )
            );

            Familia fam = familiasCache.computeIfAbsent(catNombre, name ->
                    familiaRepository.findByNombre(name).orElseGet(() ->
                            familiaRepository.save(new Familia(null, finalCatNombre, new ArrayList<>()))
                    )
            );

            String marcaNombre = null;
            for (String m : marcasLista) {
                if (n.contains(m.toLowerCase())) {
                    marcaNombre = m;
                    break;
                }
            }

            Marca marca = null;
            if (marcaNombre != null) {
                String finalMarcaNombre = marcaNombre;
                marca = marcasCache.computeIfAbsent(marcaNombre, name ->
                        marcaRepository.findByNombre(name).orElseGet(() ->
                                marcaRepository.save(new Marca(null, finalMarcaNombre, new ArrayList<>()))
                        )
                );
            }

            double precioCompra = Math.round(p.precio() * 0.70 * 100.0) / 100.0;
            boolean esPerecible = Set.of("Alimento para Perros", "Alimento para Gatos", "Golosinas y Snacks", "Farmacia y Vacunas")
                    .contains(catNombre);

            Producto producto = new Producto();
            producto.setSku(p.sku());
            producto.setNombre(p.nombre());
            producto.setPrecio(p.precio());
            producto.setStock(p.stock());
            producto.setStockMinimo(p.stockMinimo());
            producto.setDescripcion(p.descripcion());
            producto.setPrecioCompra(precioCompra);
            producto.setEsPerecible(esPerecible);
            producto.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
            producto.setProveedor(proveedor);
            producto.setCategoria(cat);
            producto.setFamilia(fam);
            producto.setMarca(marca);

            producto = productoRepository.save(producto);

            StockAlmacen stockAlmacen = new StockAlmacen(null, producto, "Disponible", producto.getStock());
            stockAlmacenRepository.save(stockAlmacen);
        }
    }
}
