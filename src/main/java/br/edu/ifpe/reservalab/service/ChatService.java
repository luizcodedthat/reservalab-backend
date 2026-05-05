package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.LlmProvider;
import br.edu.ifpe.reservalab.dto.ai.ChatMessage;
import br.edu.ifpe.reservalab.dto.ai.ChatRequest;
import br.edu.ifpe.reservalab.dto.ai.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_HISTORY_TURNS = 5;

    private final LlmProvider llmProvider;

    public ChatResponse chat(ChatRequest request) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt   = buildUserPrompt(request);

        log.debug("Chat: {} turnos de histórico incluídos", request.history().size());

        String reply = llmProvider.complete(systemPrompt, userPrompt);
        return new ChatResponse(reply);
    }

    private String buildSystemPrompt() {
        return """
            Você é um assistente virtual do ReservaLab, sistema de reservas de
            laboratórios do IFPE (Instituto Federal de Pernambuco).

            Você ajuda usuários com dúvidas sobre:
            - Como fazer, consultar e cancelar reservas de laboratórios
            - Informações sobre laboratórios disponíveis e seus recursos
            - Regras de uso e políticas de reserva
            - Como abrir chamados (tickets) de manutenção
            - Navegação geral pelo sistema

            Seja objetivo, cordial e responda sempre em português.
            Se não souber algo específico do IFPE, oriente o usuário a
            contatar a secretaria.
            Não invente dados sobre laboratórios ou reservas específicas.
            """;
    }

    private String buildUserPrompt(ChatRequest request) {
        if (request.history().isEmpty()) {
            return request.message();
        }

        // Inclui os últimos N turnos como contexto
        List<ChatMessage> recent = request.history()
                .stream()
                .skip(Math.max(0, request.history().size() - MAX_HISTORY_TURNS * 2))
                .toList();

        StringBuilder sb = new StringBuilder("Histórico da conversa:\n");
        for (ChatMessage entry : recent) {
            String label = entry.role().equals("user") ? "Usuário" : "Assistente";
            sb.append(label).append(": ").append(entry.content()).append("\n");
        }
        sb.append("\nUsuário: ").append(request.message());

        return sb.toString();
    }
}