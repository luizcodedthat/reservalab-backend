package br.edu.ifpe.reservalab.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_groups", indexes = {
        @Index(name = "idx_reservation_group_user", columnList = "created_by_user_id"),
        @Index(name = "idx_reservation_group_lab",  columnList = "laboratory_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    @Column(name = "total_occurrences", nullable = false)
    private Integer totalOccurrences;

    @Column(name = "active_occurrences", nullable = false)
    private Integer activeOccurrences;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean hasActiveOccurrences() {
        return activeOccurrences > 0;
    }

    public void decrementActiveOccurrences() {
        if (activeOccurrences <= 0) {
            throw new IllegalStateException("No active occurrences left to decrement");
        }
        this.activeOccurrences--;
    }
}