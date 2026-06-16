package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.LlmProvider;
import br.edu.ifpe.reservalab.dto.TicketClassificationRequest;
import br.edu.ifpe.reservalab.dto.TicketClassificationResponse;
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
        String systemPrompt = """
            Você é um assistente de triagem técnica do IFPE.

            Analise o cargo do solicitante, o título e a descrição do chamado
            para determinar a prioridade mais adequada.

            Valores permitidos para priority:
            - LOW: Problemas menores ou dúvidas.
            - MEDIUM: Problemas em equipamentos individuais.
            - HIGH: Problemas que afetam aulas, setores ou vários usuários.
            - URGENT: Problemas críticos que interrompem atividades institucionais.

            Regras:
            - Considere o impacto do problema como principal critério.
            - Considere também o cargo do solicitante.
            - Problemas que afetam aulas devem ter prioridade maior quando relatados por professores.
            - Problemas que afetam laboratórios, setores ou atividades institucionais podem ter prioridade elevada quando relatados por coordenadores ou gestores.
            - Não aumente a prioridade apenas pelo cargo.
            - Sempre justifique a classificação de forma objetiva.

            INSTRUÇÕES:
            - Responda APENAS com JSON.
            - Sem markdown.
            - Sem explicações fora do JSON.
            - Formato:
              {"priority":"<VALOR>", "reason":"<JUSTIFICATIVA>"}
            """;

        String userPrompt = """
            Cargo: %s
            Título: %s
            Descrição: %s
            """.formatted(
                request.role(),
                request.title(),
                request.description()
        );

        try {
            String rawResponse = llmProvider.complete(systemPrompt, userPrompt);
            return parseResponse(rawResponse);
        } catch (Exception e) {
            log.error("Erro na classificação via IA", e);

            return new TicketClassificationResponse(
                    Ticket.Priority.MEDIUM,
                    "Classificação automática indisponível."
            );
        }
    }

    private TicketClassificationResponse parseResponse(String raw) {
        String cleaned = raw
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, TicketClassificationResponse.class);
        } catch (JsonProcessingException e) {
            throw new AiProviderException(
                    "Erro ao processar JSON da IA: " + raw,
                    e
            );
        }
    }
}