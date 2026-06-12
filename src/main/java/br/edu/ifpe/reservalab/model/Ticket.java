package br.edu.ifpe.reservalab.model;

import br.edu.ifpe.reservalab.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratory_id", nullable = false)
    private Laboratory laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    // Nullable — chamado pode não estar vinculado a um computador específico
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "computer_id")
    private Computer computer;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    private LocalDateTime prazoResolucao;

    @Column(columnDefinition = "TEXT")
    private String resolutionComment;

    private LocalDateTime resolvedAt;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isSlaVencido() {
        return prazoResolucao != null
                && LocalDateTime.now().isAfter(prazoResolucao)
                && status != TicketStatus.RESOLVED
                && status != TicketStatus.CLOSED;
    }

    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
}