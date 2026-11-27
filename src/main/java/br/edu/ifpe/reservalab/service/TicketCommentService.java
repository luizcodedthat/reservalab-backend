package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.TicketCommentRequestDTO;
import br.edu.ifpe.reservalab.dto.TicketCommentResponseDTO;
import br.edu.ifpe.reservalab.exception.ForbiddenOperationException;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.TicketComment;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.TicketCommentRepository;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository        ticketRepository;
    private final UserService             userService;

    /** Lista comentários de um ticket paginados */
    @Transactional(readOnly = true)
    public Page<TicketCommentResponseDTO> findByTicket(Long ticketId, Pageable pageable) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new EntityNotFoundException("Chamado não encontrado: id=" + ticketId);
        }
        return commentRepository
                .findByTicketIdOrderByCreatedAtDesc(ticketId, pageable)
                .map(TicketCommentResponseDTO::from);
    }

    /** Cria um novo comentário em um ticket, em nome do usuário autenticado */
    @Transactional
    public TicketCommentResponseDTO create(Long ticketId, TicketCommentRequestDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: id=" + ticketId));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(userService.getCurrentUser())
                .content(dto.getContent())
                .build();

        return TicketCommentResponseDTO.from(commentRepository.save(comment));
    }

    /** Remove um comentário (autor ou SECRETARY/TECHNICIAN) */
    @Transactional
    public void delete(Long ticketId, Long commentId) {
        TicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado: id=" + commentId));

        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new EntityNotFoundException(
                    "Comentário id=" + commentId + " não pertence ao chamado id=" + ticketId);
        }

        User currentUser = userService.getCurrentUser();
        if (!comment.getUser().getId().equals(currentUser.getId()) && !UserService.isStaff(currentUser)) {
            throw new ForbiddenOperationException("Apenas o autor ou a equipe podem remover este comentário.");
        }

        commentRepository.delete(comment);
    }
}
