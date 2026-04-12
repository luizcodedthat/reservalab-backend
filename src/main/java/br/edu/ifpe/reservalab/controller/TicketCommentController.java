package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.TicketCommentRequestDTO;
import br.edu.ifpe.reservalab.dto.TicketCommentResponseDTO;
import br.edu.ifpe.reservalab.service.TicketCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService commentService;

    /**
     * GET /api/tickets/{ticketId}/comments
     * Lista comentários de um ticket com paginação
     */
    @GetMapping
    public ResponseEntity<Page<TicketCommentResponseDTO>> findByTicket(
            @PathVariable Long ticketId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.findByTicket(ticketId, pageable));
    }

    /**
     * POST /api/tickets/{ticketId}/comments
     * Cria um novo comentário no ticket
     * Body: { userId, content }
     */
    @PostMapping
    public ResponseEntity<TicketCommentResponseDTO> create(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketCommentRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(ticketId, dto));
    }

    /**
     * DELETE /api/tickets/{ticketId}/comments/{commentId}
     * Remove um comentário
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long ticketId,
            @PathVariable Long commentId
    ) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}