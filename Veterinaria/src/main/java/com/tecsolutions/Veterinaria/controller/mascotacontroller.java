package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.services.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class mascotacontroller {

    @Autowired
    private MascotaService mascotaService;

    // Tu primer endpoint se mantiene igual
    @GetMapping("/mascota")
    public Map<String, String> registrarMascota() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("estado", "atendido");
        respuesta.put("mensaje", "tu mascota ha sido registrada");
        return respuesta;
    }

    // Endpoint para buscar los productos
    @GetMapping("/buscar")
    public String buscarMascota(@RequestParam(name = "nombre") String nombre) {
        return mascotaService.verificarMascota(nombre);
    }

    // ¡NUEVO!: Aquí agregamos la ruta que te faltaba para consultar los baños
    @GetMapping("/consultar")
    public String consultarMascota(@RequestParam(name = "nombre") String nombre) {
        return mascotaService.revisarHistorial(nombre);
    }
}

