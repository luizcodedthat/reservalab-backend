package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.ai.AiProperties;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    private final RestClient restClient;
    private final String model;

    public EmbeddingService(AiProperties properties) {
        AiProperties.Embedding props = properties.embedding();
        this.model = props.model();
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Retorna vetor de 384 dimensões para o texto
    public float[] embed(String text) {
        log.debug("Gerando embedding para texto de {} chars", text.length());

        // HuggingFace retorna List<List<Float>> para feature-extraction
        List<List<Float>> response = restClient.post()
                .uri("/pipeline/feature-extraction/" + model)
                .body(Map.of("inputs", text))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.isEmpty()) {
            throw new AiProviderException("Embedding retornou vetor vazio.");
        }

        List<Float> vector = response.getFirst();
        float[] result = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) result[i] = vector.get(i);
        return result;
    }
}
