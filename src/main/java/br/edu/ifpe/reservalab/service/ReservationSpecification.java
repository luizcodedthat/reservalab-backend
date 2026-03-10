package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.enums.ReservationStatus;
import br.edu.ifpe.reservalab.model.Reservation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ReservationSpecification {

    private ReservationSpecification() {}

    public static Specification<Reservation> where(Integer ano, Long laboratoryId, LocalDateTime startDateTime, LocalDateTime endDateTime, ReservationStatus status) {
        return Specification
                .where(hasYear(ano))
                .and(hasLaboratoryId(laboratoryId));
    }

    private static Specification<Reservation> hasYear(Integer year) {
        return (root, query, cb) -> year == null ? null
                : cb.equal(cb.function("YEAR", Integer.class, root.get("startDate")), year);
    }

    private static Specification<Reservation> hasLaboratoryId(Long laboratoryId) {
        return (root, query, cb) -> laboratoryId == null ? null
                : cb.equal(root.get("laboratory").get("id"), laboratoryId);
    }


}