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

            // Sempre exclui tickets deletados
            predicates.add(cb.isTrue(root.get("active")));

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority()));
            }
            if (filter.getLabId() != null) {
                // Caminho correto após refatoração para @ManyToOne
                predicates.add(cb.equal(root.get("laboratory").get("id"), filter.getLabId()));
            }
            if (filter.getYear() != null) {
                predicates.add(cb.equal(
                        cb.function("date_part", Integer.class,
                                cb.literal("year"), root.get("createdAt")),
                        filter.getYear()
                ));
            }
            if (filter.getMonth() != null) {
                predicates.add(cb.equal(
                        cb.function("date_part", Integer.class,
                                cb.literal("month"), root.get("createdAt")),
                        filter.getMonth()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}