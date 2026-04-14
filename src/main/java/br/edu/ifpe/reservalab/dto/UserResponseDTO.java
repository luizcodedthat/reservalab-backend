package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.UserRole;
import br.edu.ifpe.reservalab.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {

    private Long id;
    private String name;
    private String username;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Mesmo padrão de factory method do TicketResponse.fromAPI() do projeto */
    public static UserResponseDTO from(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id        = user.getId();
        dto.name      = user.getName();
        dto.username  = user.getUsername();
        dto.email     = user.getEmail();
        dto.role      = user.getRole();
        dto.active    = user.isActive();
        dto.createdAt = user.getCreatedAt();
        dto.updatedAt = user.getUpdatedAt();
        return dto;
    }
}