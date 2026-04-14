package br.edu.ifpe.reservalab.dto.labSuggestion;

public record LabSuggestionResponse(
        Long labId,
        String labName,
        String reason
) {}