package br.edu.ifpe.reservalab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationRequest(

        @NotNull(message = "O laboratório é obrigatório")
        Long laboratoryId,

        @NotNull(message = "O usuário solicitante é obrigatório")
        Long requestedByUserId,

        @NotNull(message = "A data da reserva é obrigatória")
        @FutureOrPresent(message = "A data da reserva não pode ser no passado")
        LocalDate reservationDate,

        @NotBlank(message = "O propósito da reserva é obrigatório")
        @Size(max = 500, message = "O propósito deve ter no máximo 500 caracteres")
        String purpose,

        @Size(max = 1000, message = "As notas devem ter no máximo 1000 caracteres")
        String notes,

        @NotEmpty(message = "A reserva deve ter pelo menos um bloco de tempo")
        @Valid
        List<TimeBlockRequest> timeBlocks

) {
    public record TimeBlockRequest(

            @NotNull(message = "O horário de início é obrigatório")
            LocalTime startTime,

            @NotNull(message = "O horário de fim é obrigatório")
            LocalTime endTime,

            @NotNull(message = "A ordem do bloco é obrigatória")
            @Positive(message = "A ordem do bloco deve ser positiva")
            Integer blockOrder

    ) {
        @AssertTrue(message = "O horário de fim deve ser posterior ao de início")
        public boolean isEndTimeAfterStartTime() {
            if (startTime == null || endTime == null) return true;
            return endTime.isAfter(startTime);
        }

        public int durationMinutes() {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }
}