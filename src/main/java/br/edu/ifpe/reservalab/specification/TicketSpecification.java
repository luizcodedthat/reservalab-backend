package br.edu.ifpe.reservalab.specification;

import br.edu.ifpe.reservalab.dto.TicketFilter;
import br.edu.ifpe.reservalab.model.Ticket;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> withFilter(TicketFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }
            if (filter.getLabId() != null) {
                predicates.add(cb.equal(root.get("laboratoryId"), filter.getLabId()));
            }
            if (filter.getYear() != null) {
                predicates.add(cb.equal(
                        cb.function("YEAR", Integer.class, root.get("createdAt")),
                        filter.getYear()
                ));
            }
            if (filter.getMonth() != null) {
                predicates.add(cb.equal(
                        cb.function("MONTH", Integer.class, root.get("createdAt")),
                        filter.getMonth()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}