package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.LlmProvider;
import br.edu.ifpe.reservalab.dto.TicketAnalysisResponse;
import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
import br.edu.ifpe.reservalab.model.SimilarTicket;
import br.edu.ifpe.reservalab.repository.TicketEmbeddingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarTicketService {

    private static final double THRESHOLD  = 0.80; // similaridade mínima (0–1)
    private static final int    MAX        = 5;

    private static final List<TicketStatus> ABERTOS   =
            List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.PENDING);
    private static final List<TicketStatus> RESOLVIDOS =
            List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED);

    private final EmbeddingService embeddingService;
    private final TicketEmbeddingRepository embeddingRepository;
    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;

    public TicketAnalysisResponse analyze(String description) {
        float[] embedding = embeddingService.embed(description);
        List<SimilarTicket> similar = embeddingRepository.findSimilar(embedding, THRESHOLD, MAX);

        List<SimilarTicket> openDuplicates = similar.stream()
                .filter(t -> ABERTOS.contains(t.status())).toList();

        // Prioridade: se já existe aberto, avisa e bloqueia criação
        if (!openDuplicates.isEmpty()) {
            return new TicketAnalysisResponse(openDuplicates, null, false);
        }

        // Se só tem resolvidos similares, sugere solução via IA
        List<SimilarTicket> resolved = similar.stream()
                .filter(t -> RESOLVIDOS.contains(t.status())).toList();

        String suggestion = resolved.isEmpty() ? null : generateSuggestion(description, resolved);

        return new TicketAnalysisResponse(List.of(), suggestion, true);
    }

    // Chamado pelo TicketService após persistir o ticket
    public void saveEmbedding(Long ticketId, String title, String description) {
        try {
            float[] embedding = embeddingService.embed(title + " " + description);
            embeddingRepository.save(ticketId, embedding);
        } catch (Exception e) {
            // Não falha a criação do ticket por falha no embedding
            log.warn("Não foi possível gerar embedding para ticket #{}: {}", ticketId, e.getMessage());
        }
    }

    private String generateSuggestion(String description, List<SimilarTicket> resolved) {
        String systemPrompt = """
            Você é um técnico de TI do IFPE. Com base nos chamados resolvidos abaixo,
            gere um passo a passo de solução para o problema descrito.
            Seja direto e prático. Se as soluções anteriores não forem aplicáveis, diga claramente.
            """;

        String userPrompt = "Problema: %s\n\nChamados similares resolvidos:\n%s"
                .formatted(description, serializeSafely(resolved));

        try {
            return llmProvider.complete(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("Erro ao gerar sugestão de solução: {}", e.getMessage());
            return null;
        }
    }

    private String serializeSafely(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new AiProviderException("Erro ao serializar contexto para a IA.", e);
        }
    }
}