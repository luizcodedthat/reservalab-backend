package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequestDTO {

    @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
    private String name;

    @Size(min = 3, max = 100, message = "Username deve ter entre 3 e 100 caracteres")
    private String username;

    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
    private String password; // null = não altera a senha

    private UserRole role;

    private Boolean active;
}