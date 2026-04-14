package br.edu.ifpe.reservalab.exception.ai;

public class AiRateLimitException extends RuntimeException {
    public AiRateLimitException(String message) {
        super(message);
    }
}