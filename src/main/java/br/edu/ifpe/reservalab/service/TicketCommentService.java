package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.TicketCommentRequestDTO;
import br.edu.ifpe.reservalab.dto.TicketCommentResponseDTO;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.TicketComment;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.TicketCommentRepository;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import br.edu.ifpe.reservalab.repository.UserRepository;
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
    private final UserRepository          userRepository;

    /** Lista comentários de um ticket paginados */
    public Page<TicketCommentResponseDTO> findByTicket(Long ticketId, Pageable pageable) {
        return commentRepository
                .findByTicketIdOrderByCreatedAtDesc(ticketId, pageable)
                .map(TicketCommentResponseDTO::from);
    }

    /** Cria um novo comentário em um ticket */
    @Transactional
    public TicketCommentResponseDTO create(Long ticketId, TicketCommentRequestDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado."));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(user)
                .content(dto.getContent())
                .build();

        return TicketCommentResponseDTO.from(commentRepository.save(comment));
    }

    /** Atualiza o conteúdo de um comentário */
    @Transactional
    public TicketCommentResponseDTO update(Long commentId, String newContent) {
        TicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comentário não encontrado."));

        comment.setContent(newContent);
        return TicketCommentResponseDTO.from(commentRepository.save(comment));
    }

    /** Remove um comentário */
    @Transactional
    public void delete(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("Comentário não encontrado.");
        }
        commentRepository.deleteById(commentId);
    }
}