package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    Page<TicketComment> findByTicketIdOrderByCreatedAtDesc(Long ticketId, Pageable pageable);

    long countByTicketId(Long ticketId);
}