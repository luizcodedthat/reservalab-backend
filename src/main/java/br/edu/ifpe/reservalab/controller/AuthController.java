package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.AuthResponseDTO;
import br.edu.ifpe.reservalab.dto.LoginRequestDTO;
import br.edu.ifpe.reservalab.dto.RegisterRequestDTO;
import br.edu.ifpe.reservalab.dto.UserResponseDTO;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.config.JwtUtil;
import br.edu.ifpe.reservalab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil     jwtUtil;

    /**
     * POST /api/auth/login
     * Body: { email, password }
     * Retorna: { token, user }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        // Email inexistente e senha errada respondem igual (401) para não revelar contas cadastradas.
        User user = userService.findEntityByEmail(dto.getEmail())
                .filter(u -> userService.checkPassword(u, dto.getPassword()))
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(
            AuthResponseDTO.builder()
                .token(token)
                .user(UserResponseDTO.from(user))
                .build()
        );
    }

    /**
     * POST /api/auth/register
     * Body: { name, username, email, password, role }
     * Retorna: { token, user }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        UserResponseDTO userDTO = userService.register(dto);
        String token = jwtUtil.generateToken(userDTO.getEmail(), userDTO.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponseDTO.builder()
                .token(token)
                .user(userDTO)
                .build()
        );
    }

    /**
     * GET /api/auth/me
     * Header: Authorization: Bearer <token>
     * O principal é o email extraído pelo JwtFilter
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }
}