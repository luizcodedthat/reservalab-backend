package br.edu.ifpe.reservalab.specification;

import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.model.Reservation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ReservationSpecification {

    private ReservationSpecification() {}

    public static Specification<Reservation> fromFilter(ReservationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.laboratoryId() != null) {
                predicates.add(cb.equal(root.get("laboratory").get("id"), filter.laboratoryId()));
            }
            if (filter.requestedByUserId() != null) {
                predicates.add(cb.equal(root.get("requestedBy").get("id"), filter.requestedByUserId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reservationDate"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reservationDate"), filter.dateTo()));
            }
            if (filter.groupId() != null) {
                predicates.add(cb.equal(root.get("group").get("id"), filter.groupId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}