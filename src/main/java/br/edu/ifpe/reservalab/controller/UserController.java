package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.UpdateUserRequestDTO;
import br.edu.ifpe.reservalab.dto.UserResponseDTO;
import br.edu.ifpe.reservalab.enums.UserRole;
import br.edu.ifpe.reservalab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Lista todos os usuários.
     * Restrito a SECRETARY e TECHNICIAN.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * GET /api/users/{id}
     * Retorna um usuário pelo ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * GET /api/users/by-email?email=...
     * Busca usuário pelo email.
     */
    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<UserResponseDTO> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    /**
     * GET /api/users/by-username?username=...
     * Busca usuário pelo username.
     */
    @GetMapping("/by-username")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<UserResponseDTO> findByUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    /**
     * GET /api/users/by-role?role=STUDENT
     * Lista usuários por perfil.
     */
    @GetMapping("/by-role")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<List<UserResponseDTO>> findByRole(@RequestParam UserRole role) {
        return ResponseEntity.ok(userService.findAllByRole(role));
    }

    /**
     * PUT /api/users/{id}
     * Atualiza os dados de um usuário.
     * Restrito a SECRETARY e TECHNICIAN (ou o próprio usuário — controle fino pode ser adicionado depois).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO dto
    ) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    /**
     * PATCH /api/users/{id}/deactivate
     * Desativa o usuário sem removê-lo (soft delete).
     * Restrito a SECRETARY.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<UserResponseDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivate(id));
    }

    /**
     * DELETE /api/users/{id}
     * Remove o usuário permanentemente.
     * Restrito a SECRETARY.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}