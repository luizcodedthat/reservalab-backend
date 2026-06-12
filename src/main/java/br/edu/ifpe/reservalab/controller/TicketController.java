package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.*;
import br.edu.ifpe.reservalab.service.SimilarTicketService;
import br.edu.ifpe.reservalab.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SimilarTicketService similarTicketService;

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> listAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TicketResponse>> search(
            @ModelAttribute TicketFilter filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ticketService.findAllByFilter(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    // Frontend chama isso ANTES de exibir o form de criação
    @PostMapping("/analyze")
    public ResponseEntity<TicketAnalysisResponse> analyze(
            @RequestBody @Valid TicketAnalysisRequest request
    ) {
        return ResponseEntity.ok(similarTicketService.analyze(request.description()));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid TicketRequest dto) {
        TicketResponse response = ticketService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody @Valid TicketRequest dto) {
        return ResponseEntity.ok(ticketService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}