package com.chauca.Veterinaria.controller;

import com.chauca.Veterinaria.services.MascotaService;
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

    @GetMapping("/mascota")
    public Map<String, String> registrarmascota() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("estado", "atendido");
        respuesta.put("mensaje", "tu mascota ha sido registrada ");
        return respuesta;
    }

    @GetMapping("/buscar")
    public String buscarMascota(@RequestParam String nombre) {
        return mascotaService.verificarMascota(nombre);
    }
}
