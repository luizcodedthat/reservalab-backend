package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Ticket.Priority;

public record TicketClassificationResponse(
    Priority priority,
    String reason
) {}