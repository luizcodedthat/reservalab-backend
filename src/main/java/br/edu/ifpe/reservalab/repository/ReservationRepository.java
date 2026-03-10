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
            @Param("ids") List<Long> ids,
            @Param("status") ReservationStatus status
    );
}