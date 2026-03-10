package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.ReservationStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationFilter(

        Long laboratoryId,
        Long requestedByUserId,
        ReservationStatus status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateTo,

        Long groupId

) {}
