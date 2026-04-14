package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.RegisterRequestDTO;
import br.edu.ifpe.reservalab.dto.UpdateUserRequestDTO;
import br.edu.ifpe.reservalab.dto.UserResponseDTO;
import br.edu.ifpe.reservalab.enums.UserRole;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Leitura ──────────────────────────────────────────────────────────────

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        return UserResponseDTO.from(getOrThrow(id));
    }

    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return UserResponseDTO.from(user);
    }

    public UserResponseDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return UserResponseDTO.from(user);
    }

    public List<UserResponseDTO> findAllByRole(UserRole role) {
        return userRepository.findAllByRole(role)
                .stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    // ─── Criação ─────────────────────────────────────────────────────────────

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username já em uso.");
        }

        User user = User.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .active(true)
                .build();

        return UserResponseDTO.from(userRepository.save(user));
    }

    // ─── Atualização ──────────────────────────────────────────────────────────

    @Transactional
    public UserResponseDTO update(Long id, UpdateUserRequestDTO dto) {
        User user = getOrThrow(id);

        if (dto.getName() != null)     user.setName(dto.getName());
        if (dto.getRole() != null)     user.setRole(dto.getRole());
        if (dto.getActive() != null)   user.setActive(dto.getActive());

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email já cadastrado por outro usuário.");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("Username já em uso por outro usuário.");
            }
            user.setUsername(dto.getUsername());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        return UserResponseDTO.from(userRepository.save(user));
    }

    // ─── Remoção ──────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        userRepository.deleteById(id);
    }

    /** Desativa o usuário sem remover do banco (soft delete) */
    @Transactional
    public UserResponseDTO deactivate(Long id) {
        User user = getOrThrow(id);
        user.setActive(false);
        return UserResponseDTO.from(userRepository.save(user));
    }

    // ─── Helpers internos ─────────────────────────────────────────────────────

    /** Usado pelo AuthController no login (retorna a entidade, não o DTO) */
    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    private User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}