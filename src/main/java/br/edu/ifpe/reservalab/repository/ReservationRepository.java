package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.Reservation;
import br.edu.ifpe.reservalab.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.laboratory
            JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.approvedBy
            LEFT JOIN FETCH r.group
            WHERE r.id = :id
            """)
    Optional<Reservation> findByIdFetched(@Param("id") Long id);

    @Query(value = """
            SELECT r FROM Reservation r
            JOIN FETCH r.laboratory
            JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.approvedBy
            LEFT JOIN FETCH r.group
            """,
            countQuery = "SELECT COUNT(r) FROM Reservation r")
    Page<Reservation> findAllFetched(Pageable pageable);

    @Query(value = """
            SELECT r FROM Reservation r
            JOIN FETCH r.laboratory
            JOIN FETCH r.requestedBy
            LEFT JOIN FETCH r.approvedBy
            LEFT JOIN FETCH r.group
            WHERE (:laboratoryId      IS NULL OR r.laboratory.id   = :laboratoryId)
              AND (:requestedByUserId IS NULL OR r.requestedBy.id  = :requestedByUserId)
              AND (:status            IS NULL OR r.status          = :status)
              AND (:dateFrom          IS NULL OR r.reservationDate >= :dateFrom)
              AND (:dateTo            IS NULL OR r.reservationDate <= :dateTo)
              AND (:groupId           IS NULL OR r.group.id        = :groupId)
            """,
            countQuery = """
            SELECT COUNT(r) FROM Reservation r
            WHERE (:laboratoryId      IS NULL OR r.laboratory.id   = :laboratoryId)
              AND (:requestedByUserId IS NULL OR r.requestedBy.id  = :requestedByUserId)
              AND (:status            IS NULL OR r.status          = :status)
              AND (:dateFrom          IS NULL OR r.reservationDate >= :dateFrom)
              AND (:dateTo            IS NULL OR r.reservationDate <= :dateTo)
              AND (:groupId           IS NULL OR r.group.id        = :groupId)
            """)
    Page<Reservation> findAllByFilter(
            @Param("laboratoryId")      Long laboratoryId,
            @Param("requestedByUserId") Long requestedByUserId,
            @Param("status")            ReservationStatus status,
            @Param("dateFrom")          LocalDate dateFrom,
            @Param("dateTo")            LocalDate dateTo,
            @Param("groupId")           Long groupId,
            Pageable pageable
    );

    /**
     * Detecta blocos de tempo que conflitam com os da reserva sendo criada.
     *
     * Dois blocos conflitam quando se sobrepõem no tempo:
     *   existente.start < novo.end  AND  existente.end > novo.start
     *
     * Reservas CANCELLED e REJECTED são ignoradas — não ocupam o laboratório.
     *
     * Retorna os IDs das reservas conflitantes para cada bloco informado,
     * permitindo que o service monte uma mensagem de erro detalhada.
     */
    @Query("""
            SELECT r.id FROM Reservation r
            JOIN r.timeBlocks tb
            WHERE r.laboratory.id   = :laboratoryId
              AND r.reservationDate = :date
              AND r.status NOT IN :ignoredStatuses
              AND tb.startTime      < :endTime
              AND tb.endTime        > :startTime
            """)
    List<Long> findConflictingReservationIds(
            @Param("laboratoryId")    Long laboratoryId,
            @Param("date")            LocalDate date,
            @Param("startTime")       LocalTime startTime,
            @Param("endTime")         LocalTime endTime,
            @Param("ignoredStatuses") List<ReservationStatus> ignoredStatuses
    );

    @Query("""
            SELECT r.id FROM Reservation r
            WHERE r.group.id = :groupId
              AND r.status IN :cancellableStatuses
            """)
    List<Long> findCancellableIdsByGroupId(
            @Param("groupId") Long groupId,
            @Param("cancellableStatuses") List<ReservationStatus> cancellableStatuses
    );

    @Modifying
    @Query("""
            UPDATE Reservation r
            SET r.status = :status, r.version = r.version + 1
            WHERE r.id IN :ids
            """)
    int bulkUpdateStatus(
            @Param("ids")    List<Long> ids,
            @Param("status") ReservationStatus status
    );
<<<<<<< HEAD
}
=======
}
>>>>>>> c57f8e7 (feat: implementa busca e delecao parametrizadas)
