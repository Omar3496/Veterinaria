package com.tecsolutions.Veterinaria.services;

import org.springframework.stereotype.Service;

@Service
public class MascotaService {

    public String verificarMascota(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Por favor, ingresa el nombre de un producto.";
        }

        // Convertimos a minúsculas para evitar problemas de escritura
        String busqueda = nombre.toLowerCase().trim();

        if (busqueda.equals("antiparasitario")) {
            return "El producto Antiparasitario está disponible. Quedan 10 unidades.";
        } else if (busqueda.equals("antibiotico")) {
            return "El producto Antibiótico se encuentra AGOTADO.";
        } else if (busqueda.equals("vitaminas")) {
            return "El producto Vitaminas está disponible. Quedan 25 unidades.";
        } else {
            return "El producto '" + nombre + "' no está registrado en la veterinaria.";
        }
    }

    public String revisarHistorial(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Por favor, ingresa el tipo de baño a consultar (normal, medicado, antipulgas).";
        }

        String tipoBaño = nombre.toLowerCase().trim();

        if (tipoBaño.equals("medicado")) {
            return "El Baño Medicado está disponible. Incluye shampoo dermatológico especial. Quedan 5 cupos hoy.";
        } else if (tipoBaño.equals("normal")) {
            return "El Baño Normal está disponible. Incluye lavado, secado y corte de uñas. Atención inmediata.";
        } else if (tipoBaño.equals("antipulgas")) {
            return "El Baño Antipulgas se encuentra AGOTADO por falta de stock del producto antiparasitario.";
        } else {
            return "El tipo de servicio '" + nombre + "' no está disponible en nuestro catálogo de baños.";
        }
    }

}
