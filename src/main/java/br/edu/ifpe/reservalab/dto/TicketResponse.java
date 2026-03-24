package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.Ticket.Priority;
import br.edu.ifpe.reservalab.model.Ticket.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketResponse {
    private Long id;
    private Long laboratoryId;
    private Long createdByUserId;
    private Long assignedToUserId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private String resolutionComment;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketResponse from(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setLaboratoryId(ticket.getLaboratoryId());
        response.setCreatedByUserId(ticket.getCreatedByUserId());
        response.setAssignedToUserId(ticket.getAssignedToUserId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setResolutionComment(ticket.getResolutionComment());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        return response;
    }
}