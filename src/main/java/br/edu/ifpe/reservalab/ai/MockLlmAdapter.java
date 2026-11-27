package br.edu.ifpe.reservalab.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provedor de IA offline, sem rede nem API key (ai.provider=mock).
 * Devolve respostas determinísticas no mesmo formato que os serviços esperam do LLM real,
 * para que o app mobile possa ser desenvolvido contra o contrato completo da API.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock")
public class MockLlmAdapter implements LlmProvider {

    private static final Pattern FIRST_LAB = Pattern.compile("\"id\":(\\d+),\"name\":\"([^\"]+)\"");

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        log.debug("MockLlmAdapter respondendo sem chamar provedor externo");

        if (systemPrompt.contains("triagem técnica")) {
            return classifyTicket(userPrompt);
        }
        if (systemPrompt.contains("Laboratórios disponíveis")) {
            return suggestLab(systemPrompt);
        }
        return "[mock] Sou o assistente do ReservaLab em modo demonstração. "
                + "Você perguntou: \"" + lastUserLine(userPrompt) + "\". "
                + "Em produção esta resposta vem do provedor de IA configurado.";
    }

    private String classifyTicket(String userPrompt) {
        String text = userPrompt.toLowerCase();
        String priority;
        if (text.contains("incêndio") || text.contains("fumaça") || text.contains("choque")) {
            priority = "URGENT";
        } else if (text.contains("aula") || text.contains("todos") || text.contains("rede")) {
            priority = "HIGH";
        } else if (text.contains("dúvida") || text.contains("sugestão")) {
            priority = "LOW";
        } else {
            priority = "MEDIUM";
        }
        return "{\"priority\":\"%s\",\"reason\":\"[mock] Classificação por palavras-chave.\"}".formatted(priority);
    }

    private String suggestLab(String systemPrompt) {
        Matcher m = FIRST_LAB.matcher(systemPrompt);
        if (!m.find()) {
            return "{}";
        }
        return "{\"labId\":%s,\"labName\":\"%s\",\"reason\":\"[mock] Primeiro laboratório livre no período.\"}"
                .formatted(m.group(1), m.group(2));
    }

    private static String lastUserLine(String userPrompt) {
        int idx = userPrompt.lastIndexOf("Usuário: ");
        String line = idx >= 0 ? userPrompt.substring(idx + "Usuário: ".length()) : userPrompt;
        return line.strip().replace("\"", "'");
    }
}
