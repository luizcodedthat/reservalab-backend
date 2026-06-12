package br.edu.ifpe.reservalab.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ai")
@Validated
public record AiProperties(
        @NotBlank String provider,
        FreeLlm freeLlm,
        Embedding embedding
) {
    public record FreeLlm(
            @NotBlank String baseUrl,
            @NotBlank String apiKey,
            @Positive int timeoutSeconds
    ) {}

    public record Embedding(
            @NotBlank String baseUrl,
            @NotBlank String apiKey,
            @NotBlank String model
    ) {}
}