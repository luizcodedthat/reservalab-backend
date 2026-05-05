package br.edu.ifpe.reservalab.dto.ai;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ChatRequest(
        @NotBlank String message,
        List<ChatMessage> history
) {
    public ChatRequest {
        history = history != null ? history : List.of();
    }
}