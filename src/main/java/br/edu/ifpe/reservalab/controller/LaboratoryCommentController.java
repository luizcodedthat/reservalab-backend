package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.LaboratoryCommentRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import br.edu.ifpe.reservalab.service.LaboratoryCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/laboratories/{laboratoryId}/comments")
@RequiredArgsConstructor
public class LaboratoryCommentController {

    private final LaboratoryCommentService commentService;

    @GetMapping
    public ResponseEntity<Page<LaboratoryCommentResponse>> findByLaboratory(
            @PathVariable Long laboratoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.findByLaboratory(laboratoryId, pageable));
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
                .buildAndExpand(response.id())
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