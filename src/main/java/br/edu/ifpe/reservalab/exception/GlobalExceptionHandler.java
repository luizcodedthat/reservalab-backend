package br.edu.ifpe.reservalab.exception;

import br.edu.ifpe.reservalab.dto.ai.NoLabsAvailableException;
import br.edu.ifpe.reservalab.dto.error.ConflictErrorResponse;
import br.edu.ifpe.reservalab.dto.error.ConflictErrorResponse.ConflictItem;
import br.edu.ifpe.reservalab.dto.error.ErrorResponse;
import br.edu.ifpe.reservalab.dto.error.ValidationErrorResponse;
import br.edu.ifpe.reservalab.dto.error.ValidationErrorResponse.FieldViolation;
import br.edu.ifpe.reservalab.exception.ai.AiProviderException;
import br.edu.ifpe.reservalab.exception.ai.AiRateLimitException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(ConflictingReservationException.class)
    public ResponseEntity<ConflictErrorResponse> handleConflict(ConflictingReservationException ex) {
        List<ConflictItem> items = ex.getConflicts().stream()
                .map(c -> new ConflictItem(
                        c.date(),
                        c.requestedStart(),
                        c.requestedEnd(),
                        c.conflictingReservationId()
                ))
                .toList();

        ConflictErrorResponse body = new ConflictErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflito de horário",
                "Um ou mais blocos de tempo conflitam com reservas existentes.",
                items
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldViolation> violations = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String field   = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
                    String message = error.getDefaultMessage();
                    return new FieldViolation(field, message);
                })
                .toList();

        ValidationErrorResponse body = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                violations
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<ErrorResponse> handleAiProviderException(AiProviderException ex) {
        log.error("Erro no provedor de IA: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Serviço de IA indisponível. Tente novamente."
                ));
    }

    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleAiRateLimit(AiRateLimitException ex) {
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "40")
                .body(ErrorResponse.of(
                        HttpStatus.TOO_MANY_REQUESTS,
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(NoLabsAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNoLabsAvailable(NoLabsAvailableException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Serviço de IA indisponível. Tente novamente."
                ));
    }
}