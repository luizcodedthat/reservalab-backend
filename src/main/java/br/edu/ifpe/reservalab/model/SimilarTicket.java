package br.edu.ifpe.reservalab.model;

import br.edu.ifpe.reservalab.enums.TicketStatus;

public record SimilarTicket(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Ticket.Priority priority,
        double similarity
) {}