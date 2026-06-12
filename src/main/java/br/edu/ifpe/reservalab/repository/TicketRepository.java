package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByIdAndActiveTrue(Long id);

    Page<Ticket> findAllByActiveTrue(Pageable pageable);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.active = true
          AND t.prazoResolucao IS NOT NULL
          AND t.prazoResolucao < :now
          AND t.status IN :activeStatuses
        """)
    List<Ticket> findSlaVencidos(
            @Param("now")            LocalDateTime now,
            @Param("activeStatuses") List<TicketStatus> activeStatuses
    );
}