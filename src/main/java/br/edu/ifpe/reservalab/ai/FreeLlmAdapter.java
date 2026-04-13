package br.edu.ifpe.reservalab.ai;

import br.edu.ifpe.reservalab.dto.ai.FreeLlmRequest;
import br.edu.ifpe.reservalab.dto.ai.FreeLlmResponse;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
import br.edu.ifpe.reservalab.exception.ai.AiRateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "freeapi")
public class FreeLlmAdapter implements LlmProvider {

    private static final String CHAT_ENDPOINT = "/api/v1/chat";

    private final RestClient restClient;

    public FreeLlmAdapter(AiProperties properties) {
        AiProperties.FreeLlm props = properties.freeLlm();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(props.timeoutSeconds()));
        factory.setConnectTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        String combined = systemPrompt + "\n\n" + userPrompt;

        log.debug("Enviando requisição para FreeLLM. Tamanho: {} chars", combined.length());

        FreeLlmResponse response = restClient.post()
                .uri(CHAT_ENDPOINT)
                .body(new FreeLlmRequest(combined))
                .retrieve()
                .onStatus(status -> status.value() == 429, (req, res) -> {
                    throw new AiRateLimitException("Rate limit atingido. Aguarde 40 segundos.");
                })
                .onStatus(status -> status.value() == 401, (req, res) -> {
                    throw new AiProviderException("API key inválida ou ausente.");
                })
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new AiProviderException("Erro do provedor de IA: HTTP " + res.getStatusCode());
                })
                .body(FreeLlmResponse.class);

        assert response != null;
        String content = response.extractContent();
        log.debug("Resposta recebida: {} chars", content.length());

        return content;
    }
}