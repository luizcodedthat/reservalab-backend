package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.enums.UserRole;
import br.edu.ifpe.reservalab.model.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketRequest(
        @NotNull Long laboratoryId,
        @NotNull Long createdByUserId,
        Long computerId,            // preenchido ao escanear QR Code
        @NotBlank String title,
        @NotBlank String description,
        UserRole userRole,          // temporário até JWT estar implementado
        // campos usados apenas em update:
        TicketStatus status,
        Ticket.Priority priority,
        String resolutionComment
) {}