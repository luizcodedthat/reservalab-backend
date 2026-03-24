package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Ticket.Priority;
import br.edu.ifpe.reservalab.enums.TicketStatus;
import lombok.Data;

@Data
public class TicketFilter {
    private Long labId;
    private TicketStatus status;
    private Priority priority;
    private Integer year;
    private Integer month;
}