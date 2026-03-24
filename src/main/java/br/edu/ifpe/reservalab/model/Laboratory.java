package br.edu.ifpe.reservalab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "laboratories",
        indexes = {
                @Index(name = "idx_laboratory_active", columnList = "active"),
                @Index(name = "idx_laboratory_code", columnList = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "description")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Laboratory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @NotNull
    @Min(0)
    @Builder.Default
    @Column(name = "computer_count", nullable = false)
    private Integer computerCount = 0;

    @NotNull
    @Min(1)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Size(max = 100)
    @Column(name = "building", length = 100)
    private String building;

    @Size(max = 20)
    @Column(name = "floor", length = 20)
    private String floor;

    @Builder.Default
    @Column(name = "active", nullable = false)
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
        return this.capacity != null && this.capacity >= requiredSeats;
    }
}