package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketClassificationRequest(
    @NotBlank String title,
    @NotBlank String description
) {}