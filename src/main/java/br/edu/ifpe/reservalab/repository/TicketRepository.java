package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.Ticket.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByLaboratoryId(Long laboratoryId);

    List<Ticket> findByStatusAndLaboratoryId(Status status, Long laboratoryId);

    @Query("SELECT t FROM Ticket t WHERE YEAR(t.createdAt) = :year")
    List<Ticket> findByYear(@Param("year") int year);

    @Query("SELECT t FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Ticket> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
