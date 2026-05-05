package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.*;

public record LaboratoryRequest(

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100)
    String name,

    @NotBlank(message = "O código é obrigatório")
    @Size(max = 20)
    String code,

    @Size(max = 500)
    String description,

    @NotNull(message = "A capacidade é obrigatória")
    @Positive(message = "A capacidade deve ser positiva")
    Integer capacity,

    Integer computerCount,

    @Size(max = 50)
    String building,

    @Size(max = 20)
    String floor,

    Boolean active
) {}