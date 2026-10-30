package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemografiaItemDTO {

    private String name;
    private Long value;

    @JsonProperty("especie")
    public String getEspecie() {
        return name;
    }

    @JsonProperty("cantidad")
    public Long getCantidad() {
        return value;
    }
}
