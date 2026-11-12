package com.tecsolutions.Veterinaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class DashboardProfesorTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void testVerDashboardCritico() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REPORTE DE PUNTOS CRÍTICOS")))
                .andExpect(content().string(containsString("Perros registrados")))
                .andExpect(content().string(containsString("Mascota más longeva")))
                .andExpect(content().string(containsString("Michi (Gato - Siames)")))
                .andExpect(content().string(containsString("Amoxicilina")))
                .andExpect(content().string(containsString("Naproxeno")))
                .andExpect(content().string(containsString("Paracetamol")));
    }

    @Test
    void testContarPorEspecieEndpoint() throws Exception {
        mockMvc.perform(get("/dashboard/mascotas/contar?especie=Perro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especie").value("Perro"))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(3)));

        mockMvc.perform(get("/dashboard/mascotas/contar?especie=Gato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especie").value("Gato"))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(2)));
    }

    @Test
    void testMascotaMasLongevaEndpoint() throws Exception {
        mockMvc.perform(get("/dashboard/mascotas/mas-longeva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Michi"))
                .andExpect(jsonPath("$.especie").value("Gato"))
                .andExpect(jsonPath("$.edad_anios", greaterThanOrEqualTo(10)));
    }

    @Test
    void testMedicamentoStockEndpoint() throws Exception {
        mockMvc.perform(get("/dashboard/medicamentos/stock?nombre=Naproxeno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamento").value("Naproxeno"))
                .andExpect(jsonPath("$.stock_total").value(6));

        mockMvc.perform(get("/dashboard/medicamentos/stock?nombre=Paracetamol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicamento").value("Paracetamol"))
                .andExpect(jsonPath("$.stock_total").value(15));
    }

    @Test
    void testMedicamentosCriticosEndpoint() throws Exception {
        mockMvc.perform(get("/dashboard/medicamentos/criticos?limite=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].nombre", hasItem(containsString("Naproxeno"))));
    }

    @Test
    void testBuscarMedicamentoJSONEndpoint() throws Exception {
        mockMvc.perform(get("/dashboard/medicamentos/buscar?nombre=Naproxeno&cantidad=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre", containsString("Naproxeno")))
                .andExpect(jsonPath("$[0].stock").value(6));
    }
}
