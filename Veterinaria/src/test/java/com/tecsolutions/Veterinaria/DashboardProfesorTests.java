package com.tecsolutions.Veterinaria;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(content().string(containsString("Amoxicilina")));
    }

    @Test
    void testBuscarMedicamentoCritico() throws Exception {
        mockMvc.perform(get("/dashboard/buscar?nombre=Amoxicilina&stockMaximo=20"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("RESULTADOS DE BÚSQUEDA")))
                .andExpect(content().string(containsString("Amoxicilina")));
    }
}
