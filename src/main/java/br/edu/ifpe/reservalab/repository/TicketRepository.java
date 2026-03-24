package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByLaboratoryId(Long laboratoryId);

    List<Ticket> findByStatusAndLaboratoryId(TicketStatus status, Long laboratoryId);

    @Query("SELECT t FROM Ticket t WHERE YEAR(t.createdAt) = :year")
    List<Ticket> findByYear(@Param("year") int year);

    @Query("SELECT t FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Ticket> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}