package br.edu.ifpe.reservalab.dto.ai;

public class NoLabsAvailableException extends RuntimeException {
    public NoLabsAvailableException(String message) {
        super(message);
    }
}