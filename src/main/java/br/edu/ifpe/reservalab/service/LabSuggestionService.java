package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.LlmProvider;
import br.edu.ifpe.reservalab.dto.LabAvailabilityContext;
import br.edu.ifpe.reservalab.dto.ai.NoLabsAvailableException;
import br.edu.ifpe.reservalab.dto.labSuggestion.LabSuggestionRequest;
import br.edu.ifpe.reservalab.dto.labSuggestion.LabSuggestionResponse;
import br.edu.ifpe.reservalab.enums.ReservationStatus;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.repository.ReservationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabSuggestionService {

    private static final List<ReservationStatus> IGNORED_ON_CONFLICT =
            List.of(ReservationStatus.CANCELLED);

    private final LlmProvider llmProvider;
    private final LaboratoryRepository laboratoryRepository;
    private final ReservationRepository reservationRepository;
    private final ObjectMapper objectMapper;

    public LabSuggestionResponse suggest(LabSuggestionRequest request) {
        List<LabAvailabilityContext> availableLabs =
                fetchAvailableLabs(request.date(), request.startTime(), request.endTime());

        if (availableLabs.isEmpty()) {
            throw new NoLabsAvailableException("Nenhum laboratório disponível para o período solicitado.");
        }

        String systemPrompt = buildSystemPrompt(availableLabs);
        String rawResponse = llmProvider.complete(systemPrompt, request.userPrompt());

        return parseResponse(rawResponse);
    }

    private List<LabAvailabilityContext> fetchAvailableLabs(
            LocalDate date, LocalTime start, LocalTime end
    ) {
        return laboratoryRepository.findAll().stream()
                .filter(lab -> reservationRepository.findConflictingReservationIds(
                        lab.getId(), date, start, end, IGNORED_ON_CONFLICT
                ).isEmpty())
                .map(lab -> new LabAvailabilityContext(
                        lab.getId(),
                        lab.getName(),
                        lab.getCapacity(),
                        lab.getComputerCount()
                ))
                .toList();
    }

    private String buildSystemPrompt(List<LabAvailabilityContext> labs) {
        return """
            Você é um assistente de reservas de laboratórios do IFPE.
            Analise o pedido do usuário e sugira o laboratório mais adequado \
            dentre os disponíveis abaixo.

            Laboratórios disponíveis (já filtrados por disponibilidade no período):
            %s

            INSTRUÇÕES DE RESPOSTA (siga rigorosamente):
            - Responda APENAS com o objeto JSON abaixo, sem nenhum texto antes ou depois
            - Não use blocos de código markdown (sem ```)
            - Não inclua explicações fora do JSON
            - Use exatamente este formato:
            {"labId":<id>,"labName":"<nome exato>","reason":"<justificativa em 1-2 frases>"}
            """.formatted(serializeSafely(labs));
    }

    private LabSuggestionResponse parseResponse(String raw) {
        String cleaned = raw
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, LabSuggestionResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Falha ao parsear resposta da IA. Raw: [{}]", raw);
            throw new AiProviderException("Resposta da IA em formato inesperado.", e);
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