package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Ticket.Priority;
import br.edu.ifpe.reservalab.enums.TicketStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Corpo de criação (POST) e atualização parcial (PUT) de chamados.
 * O criador é sempre o usuário autenticado; a prioridade inicial é definida pela IA.
 */
@Data
public class TicketDTO {
    private Long laboratoryId;
    private Long assignedToUserId;

    @Size(max = 255)
    private String title;

    private String description;
    private TicketStatus status;
    private Priority priority;
    private String resolutionComment;
}
