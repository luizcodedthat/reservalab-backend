    package br.edu.ifpe.reservalab.model;

    import br.edu.ifpe.reservalab.model.Laboratory;
    import br.edu.ifpe.reservalab.enums.ReservationStatus;
    import br.edu.ifpe.reservalab.model.ReservationGroup;
    import br.edu.ifpe.reservalab.model.User;
    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;

    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "reservations", indexes = {
            @Index(name = "idx_reservation_lab_date", columnList = "laboratory_id, reservation_date"),
            @Index(name = "idx_reservation_group",    columnList = "reservation_group_id"),
            @Index(name = "idx_reservation_user",     columnList = "requested_by_user_id"),
            @Index(name = "idx_reservation_status",   columnList = "status")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Reservation {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "laboratory_id", nullable = false)
        private Laboratory laboratory;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "requested_by_user_id", nullable = false)
        private User requestedBy;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "approved_by_user_id")
        private User approvedBy;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "reservation_group_id")
        private ReservationGroup group;

        @Column(name = "reservation_date", nullable = false)
        private LocalDate reservationDate;

        @Column(name = "purpose", nullable = false, length = 500)
        private String purpose;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private ReservationStatus status = ReservationStatus.PENDING;

        @Column(name = "rejection_reason", length = 500)
        private String rejectionReason;

        @Column(name = "notes", columnDefinition = "text")
        private String notes;

        @Column(name = "total_duration_minutes", nullable = false)
        private Integer totalDurationMinutes;

        @Column(name = "occurrence_number")
        private Integer occurrenceNumber;

        @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("blockOrder ASC")
        @Builder.Default
        private List<ReservationTimeBlock> timeBlocks = new ArrayList<>();

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Version
        @Column(name = "version", nullable = false)
        private Long version;

        public boolean isCancellable() {
            return status == ReservationStatus.PENDING || status == ReservationStatus.APPROVED;
        }
    }