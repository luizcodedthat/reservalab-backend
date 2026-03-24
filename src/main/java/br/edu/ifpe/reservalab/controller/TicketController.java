package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.TicketDTO;
import br.edu.ifpe.reservalab.dto.TicketFilter;
import br.edu.ifpe.reservalab.dto.TicketResponse;
import br.edu.ifpe.reservalab.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

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

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody TicketDTO dto) {
        return ResponseEntity.status(201).body(ticketService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

