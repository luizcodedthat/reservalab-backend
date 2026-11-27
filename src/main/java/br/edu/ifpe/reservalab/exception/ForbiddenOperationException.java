package br.edu.ifpe.reservalab.exception;

/** O usuário está autenticado, mas não pode agir sobre o recurso (ex.: reserva de outra pessoa). */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
