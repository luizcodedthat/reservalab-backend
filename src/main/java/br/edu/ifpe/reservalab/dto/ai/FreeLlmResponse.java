package br.edu.ifpe.reservalab.dto.ai;

import br.edu.ifpe.reservalab.exception.ai.AiProviderException;

public record FreeLlmResponse(
        boolean success,
        String response,
        String tier
) {
    public String extractContent() {
        if (!success || response == null || response.isBlank()) {
            throw new AiProviderException("Resposta inválida ou vazia do provedor de IA.");
        }
        return response;
    }
}