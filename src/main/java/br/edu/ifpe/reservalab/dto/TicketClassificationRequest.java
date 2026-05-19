package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.UserRole;

public record TicketClassificationRequest(
        String title,
        String description,
        UserRole userRole   // recebido do token JWT (por ora, do request)
) {}