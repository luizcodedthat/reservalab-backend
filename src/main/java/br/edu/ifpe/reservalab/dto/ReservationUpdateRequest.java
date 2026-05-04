package br.edu.ifpe.reservalab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationUpdateRequest(

    @NotNull(message = "O laboratório é obrigatório")
    Long laboratoryId,

    @NotNull(message = "A data da reserva é obrigatória")
    @FutureOrPresent(message = "A data da reserva não pode ser no passado")
    LocalDate reservationDate,

    @NotBlank(message = "O propósito da reserva é obrigatório")
    @Size(max = 500, message = "O propósito deve ter no máximo 500 caracteres")
    String purpose,

    @Size(max = 1000)
    String notes,

    @NotEmpty(message = "A reserva deve ter pelo menos um bloco de tempo")
    @Valid
    List<ReservationRequest.TimeBlockRequest> timeBlocks  // reusa o record interno já existente

) {}