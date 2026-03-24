package br.edu.ifpe.reservalab.dto.error;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        List<FieldViolation> violations
) {
    public record FieldViolation(String field, String message) {}
}