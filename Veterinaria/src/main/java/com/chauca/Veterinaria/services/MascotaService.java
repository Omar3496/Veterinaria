package com.chauca.Veterinaria.services;

import org.springframework.stereotype.Service;

@Service
public class MascotaService {
    public String verificarMascota(String nombre){
        return "El veterinario dice: la mascota " + nombre+ " Está lista para su atención médica";
    }
}
