package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.ComputerRequest;
import br.edu.ifpe.reservalab.dto.ComputerResponse;
import br.edu.ifpe.reservalab.service.ComputerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/computers")
@RequiredArgsConstructor
public class ComputerController {

    private final ComputerService computerService;

    // Usado pelo frontend ao escanear QR Code (patrimônio encodado no QR)
    @GetMapping("/by-patrimonio/{patrimonio}")
    public ResponseEntity<ComputerResponse> findByPatrimonio(@PathVariable String patrimonio) {
        return ResponseEntity.ok(computerService.findByPatrimonio(patrimonio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComputerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(computerService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ComputerResponse>> findByLaboratory(
            @RequestParam Long laboratoryId
    ) {
        return ResponseEntity.ok(computerService.findByLaboratory(laboratoryId));
    }

    @PostMapping
    public ResponseEntity<ComputerResponse> create(@RequestBody @Valid ComputerRequest request) {
        ComputerResponse response = computerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        computerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}