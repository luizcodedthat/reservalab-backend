package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.LaboratoryCommentRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.service.LaboratoryCommentService;
import br.edu.ifpe.reservalab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/laboratories/{laboratoryId}/comments")
@RequiredArgsConstructor
public class LaboratoryCommentController {

    private final LaboratoryCommentService commentService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<LaboratoryCommentResponse>> findByLaboratory(
            @PathVariable Long laboratoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal String email
    ) {
        Long userId = null;

        if (email != null) {
            User user = userService.getEntityByEmail(email); // busca usuário logado
            userId = user.getId();
        }

        Page<LaboratoryCommentResponse> comments = commentService.findByLaboratory(laboratoryId, pageable, userId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<LaboratoryCommentResponse> create(
            @PathVariable Long laboratoryId,
            @RequestBody @Valid LaboratoryCommentRequest request
    ) {
        LaboratoryCommentResponse response = commentService.create(laboratoryId, request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long laboratoryId,
            @PathVariable Long commentId
    ) {
        commentService.delete(laboratoryId, commentId);
        return ResponseEntity.noContent().build();
    }
}