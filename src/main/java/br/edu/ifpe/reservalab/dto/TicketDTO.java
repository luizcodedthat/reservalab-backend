package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Ticket.Priority;
import br.edu.ifpe.reservalab.enums.TicketStatus;
import lombok.Data;

@Data
public class TicketDTO {
    private Long laboratoryId;
    private Long createdByUserId;
    private Long assignedToUserId;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private String resolutionComment;
}