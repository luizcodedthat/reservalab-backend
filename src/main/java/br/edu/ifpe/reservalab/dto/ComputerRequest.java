package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComputerRequest(
        @NotBlank String patrimonio,
        @NotNull Long laboratoryId,
        String ip,
        String processador,
        String ram,
        String so
) {}