package br.edu.ifpe.reservalab.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketAnalysisRequest(@NotBlank String description) {}