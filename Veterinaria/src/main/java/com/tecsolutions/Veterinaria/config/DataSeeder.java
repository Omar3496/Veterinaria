package com.tecsolutions.Veterinaria.config;

import com.tecsolutions.Veterinaria.model.Cita;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Propietario;
import com.tecsolutions.Veterinaria.model.Veterinario;
import com.tecsolutions.Veterinaria.repository.CitaRepository;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.PropietarioRepository;
import com.tecsolutions.Veterinaria.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public void run(String... args) {
        if (propietarioRepository.count() > 0) {
            return;
        }

        // 1. Propietarios
        Propietario prop1 = new Propietario(null, "Carlos", "Mendoza", "987654321", "carlos.mendoza@email.com", "Av. Javier Prado 123, Lima", null);
        Propietario prop2 = new Propietario(null, "María", "Gómez", "912345678", "maria.gomez@email.com", "Calle Los Pinos 456, Miraflores", null);
        Propietario prop3 = new Propietario(null, "Luis", "Paredes", "998877665", "luis.paredes@email.com", "Jr. Las Flores 789, Surco", null);
        propietarioRepository.saveAll(List.of(prop1, prop2, prop3));

        // 2. Veterinarios
        Veterinario vet1 = new Veterinario(null, "Ana", "Salazar", "Medicina General", "955443322", "ana.salazar@veterinaria.com", "CMVP-1024", null);
        Veterinario vet2 = new Veterinario(null, "Roberto", "Ríos", "Cirugía", "966554433", "roberto.rios@veterinaria.com", "CMVP-2048", null);
        Veterinario vet3 = new Veterinario(null, "Lucía", "Vargas", "Dermatología", "977665544", "lucia.vargas@veterinaria.com", "CMVP-3096", null);
        veterinarioRepository.saveAll(List.of(vet1, vet2, vet3));

        // 3. Mascotas
        Mascota masc1 = new Mascota(null, "Max", "Perro", "Labrador", 4, prop1, null);
        Mascota masc2 = new Mascota(null, "Luna", "Gato", "Siamés", 2, prop1, null);
        Mascota masc3 = new Mascota(null, "Rocky", "Perro", "Bulldog Francés", 3, prop2, null);
        Mascota masc4 = new Mascota(null, "Michi", "Gato", "Persa", 1, prop3, null);
        mascotaRepository.saveAll(List.of(masc1, masc2, masc3, masc4));

        // 4. Citas
        Cita cita1 = new Cita(
                null,
                LocalDateTime.of(2026, 11, 2, 10, 0),
                "Consulta general y control de peso",
                "Pendiente",
                "Paciente activo, revisar calendario de vacunas",
                masc1,
                vet1
        );

        Cita cita2 = new Cita(
                null,
                LocalDateTime.of(2026, 11, 3, 15, 30),
                "Revisión dermatológica por alergia",
                "Pendiente",
                "Presenta irritación leve en la piel",
                masc2,
                vet3
        );

        Cita cita3 = new Cita(
                null,
                LocalDateTime.of(2026, 10, 25, 9, 0),
                "Vacunación anual séxtuple",
                "Completada",
                "Se aplicó vacuna sin reacciones adversas",
                masc3,
                vet1
        );

        Cita cita4 = new Cita(
                null,
                LocalDateTime.of(2026, 11, 5, 11, 0),
                "Evaluación preoperatoria de esterilización",
                "Pendiente",
                "Asistir en ayunas de 8 horas",
                masc4,
                vet2
        );

        citaRepository.saveAll(List.of(cita1, cita2, cita3, cita4));
    }
}

