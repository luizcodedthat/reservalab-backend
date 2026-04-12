package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.TicketComment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketCommentResponseDTO {

    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketCommentResponseDTO from(TicketComment comment) {
        TicketCommentResponseDTO dto = new TicketCommentResponseDTO();
        dto.id         = comment.getId();
        dto.ticketId   = comment.getTicket().getId();
        dto.authorId   = comment.getUser().getId();
        dto.authorName = comment.getUser().getName();
        dto.content    = comment.getContent();
        dto.createdAt  = comment.getCreatedAt();
        dto.updatedAt  = comment.getUpdatedAt();
        return dto;
    }
}