package br.edu.ifpe.reservalab.ai;

public interface LlmProvider {
    String complete(String systemPrompt, String userPrompt);
}