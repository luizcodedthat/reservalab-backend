package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.LaboratoryRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import br.edu.ifpe.reservalab.service.LaboratoryService;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/laboratories")
@RequiredArgsConstructor
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @GetMapping
    public ResponseEntity<Page<LaboratoryResponse>> listAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(laboratoryService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaboratoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<LaboratoryResponse> create(
            @RequestBody @Valid LaboratoryRequest request) {
        LaboratoryResponse response = laboratoryService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<LaboratoryResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid LaboratoryRequest request) {
        return ResponseEntity.ok(laboratoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        laboratoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        laboratoryService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SECRETARY', 'TECHNICIAN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        laboratoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}