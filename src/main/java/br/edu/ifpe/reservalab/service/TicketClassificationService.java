package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.LlmProvider;
import br.edu.ifpe.reservalab.dto.TicketClassificationRequest;
import br.edu.ifpe.reservalab.dto.TicketClassificationResponse;
import br.edu.ifpe.reservalab.enums.UserRole;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
import br.edu.ifpe.reservalab.model.Ticket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketClassificationService {

    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;

    public TicketClassificationResponse classify(TicketClassificationRequest request) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt   = "Título: %s\nDescrição: %s"
                .formatted(request.title(), request.description());

        try {
            String raw = llmProvider.complete(systemPrompt, userPrompt);
            TicketClassificationResponse aiResult = parseResponse(raw);

            // Boost de prioridade baseado no cargo do solicitante
            Ticket.Priority boosted = boostByRole(aiResult.priority(), request.userRole());
            return new TicketClassificationResponse(boosted, aiResult.reason());

        } catch (Exception e) {
            log.error("Erro na classificação via IA: {}", e.getMessage());
            return new TicketClassificationResponse(Ticket.Priority.MEDIUM, "Classificação automática indisponível.");
        }
    }

    // PROFESSOR e SECRETARY sobem um nível em relação ao STUDENT para o mesmo problema
    private Ticket.Priority boostByRole(Ticket.Priority priority, UserRole role) {
        if (role == null || role == UserRole.STUDENT) return priority;
        return switch (priority) {
            case LOW    -> Ticket.Priority.MEDIUM;
            case MEDIUM -> Ticket.Priority.HIGH;
            case HIGH   -> Ticket.Priority.URGENT;
            case URGENT -> Ticket.Priority.URGENT;
        };
    }

    private String buildSystemPrompt() {
        return """
            Você é um assistente de triagem técnica do IFPE.
            Analise o título e a descrição de um chamado e determine a prioridade.

            Valores permitidos:
            - LOW: Problemas menores ou dúvidas.
            - MEDIUM: Problemas em equipamentos individuais (ex: um PC com defeito).
            - HIGH: Problemas que afetam a aula ou muitos usuários.
            - URGENT: Problemas críticos (ex: laboratório todo sem internet).

            Responda APENAS com JSON, sem markdown:
            {"priority":"<VALOR>","reason":"<JUSTIFICATIVA>"}
            """;
    }

    private TicketClassificationResponse parseResponse(String raw) {
        String cleaned = raw.replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "").trim();
        try {
            return objectMapper.readValue(cleaned, TicketClassificationResponse.class);
        } catch (JsonProcessingException e) {
            throw new AiProviderException("Resposta da IA em formato inesperado: " + raw, e);
        }
    }
}