package br.edu.ifpe.reservalab.dto.error;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ConflictErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<ConflictItem> conflicts
) {
    public record ConflictItem(
            LocalDate date,
            LocalTime requestedStart,
            LocalTime requestedEnd,
            Long conflictingReservationId
    ) {}
}