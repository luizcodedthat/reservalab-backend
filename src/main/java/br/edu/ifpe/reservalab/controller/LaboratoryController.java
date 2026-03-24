package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import br.edu.ifpe.reservalab.service.LaboratoryService;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/laboratories")
@RequiredArgsConstructor
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @GetMapping
    public ResponseEntity<Page<LaboratoryResponse>> listAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ResponseEntity.ok(laboratoryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaboratoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.findById(id));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<LaboratoryCommentResponse>> findComments(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(laboratoryService.findComments(id, pageable));
    }
}