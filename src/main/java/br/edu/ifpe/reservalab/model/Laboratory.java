package br.edu.ifpe.reservalab.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "laboratories", indexes = {
        @Index(name = "idx_laboratory_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Laboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "computer_count", nullable = false)
    @Builder.Default
    private Integer computerCount = 0;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "building", length = 100)
    private String building;

    @Column(name = "floor", length = 20)
    private String floor;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isActive() {
        return active;
    }

    public boolean hasAvailableCapacity(int requiredSeats) {
        return this.capacity >= requiredSeats;
    }
}