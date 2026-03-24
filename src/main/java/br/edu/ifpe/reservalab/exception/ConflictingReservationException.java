package br.edu.ifpe.reservalab.exception;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ConflictingReservationException extends RuntimeException {

    private final List<ConflictDetail> conflicts;

    public ConflictingReservationException(List<ConflictDetail> conflicts) {
        super("Conflito de horário detectado: " + conflicts.size() + " bloco(s) em conflito");
        this.conflicts = conflicts;
    }

    public List<ConflictDetail> getConflicts() {
        return conflicts;
    }

    public record ConflictDetail(
            LocalDate date,
            LocalTime requestedStart,
            LocalTime requestedEnd,
            LocalTime conflictingStart,
            LocalTime conflictingEnd,
            Long conflictingReservationId
    ) {}
}