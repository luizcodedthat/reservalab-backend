package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.LlmProvider;
import br.edu.ifpe.reservalab.dto.TicketClassificationRequest;
import br.edu.ifpe.reservalab.dto.TicketClassificationResponse;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
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
        String systemPrompt = """
            Você é um assistente de triagem técnica do IFPE.
            Analise o título e a descrição de um chamado e determine a prioridade.

            Valores permitidos para priority:
            - LOW: Problemas menores ou dúvidas.
            - MEDIUM: Problemas em equipamentos individuais (ex: um PC com defeito).
            - HIGH: Problemas que afetam a aula ou muitos usuários.
            - URGENT: Problemas críticos (ex: laboratório todo sem internet).

            INSTRUÇÕES:
            - Responda APENAS com JSON. Sem explicações fora do objeto.
            - Formato: {"priority":"<VALOR>", "reason":"<JUSTIFICATIVA>"}
            """;

        String userPrompt = "Título: %s\nDescrição: %s".formatted(request.title(), request.description());

        try {
            String rawResponse = llmProvider.complete(systemPrompt, userPrompt);
            return parseResponse(rawResponse);
        } catch (Exception e) {
            log.error("Erro na classificação via IA: {}", e.getMessage());
            
            return new TicketClassificationResponse(br.edu.ifpe.reservalab.model.Ticket.Priority.MEDIUM, "Classificação automática indisponível.");
        }
    }

    private TicketClassificationResponse parseResponse(String raw) {
        String cleaned = raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
        try {
            return objectMapper.readValue(cleaned, TicketClassificationResponse.class);
        } catch (JsonProcessingException e) {
            throw new AiProviderException("Erro ao processar JSON da IA: " + raw, e);
        }
    }
}