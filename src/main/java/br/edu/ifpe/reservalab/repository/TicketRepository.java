package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
<<<<<<< HEAD
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(Status status);

    List<Ticket> findByLaboratoryId(Long laboratoryId);

    List<Ticket> findByStatusAndLaboratoryId(Status status, Long laboratoryId);

    @Query("SELECT t FROM Ticket t WHERE YEAR(t.createdAt) = :year")
    List<Ticket> findByYear(@Param("year") int year);

    @Query("SELECT t FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Ticket> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
=======
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
}
>>>>>>> 3c97441 (fix: correcao get e delete)
