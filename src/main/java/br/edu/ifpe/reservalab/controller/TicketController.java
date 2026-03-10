package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.TicketDTO;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.Ticket.Status;
import br.edu.ifpe.reservalab.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> listarTodos() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    @GetMapping("/filtrar/status")
    public ResponseEntity<List<Ticket>> filtrarPorStatus(@RequestParam Status status) {
        return ResponseEntity.ok(ticketService.filtrarPorStatus(status));
    }

    @GetMapping("/filtrar/laboratorio")
    public ResponseEntity<List<Ticket>> filtrarPorLaboratorio(@RequestParam Long labId) {
        return ResponseEntity.ok(ticketService.filtrarPorLaboratorio(labId));
    }

    @GetMapping("/filtrar/ano")
    public ResponseEntity<List<Ticket>> filtrarPorAno(@RequestParam int year) {
        return ResponseEntity.ok(ticketService.findByYear(year));
    }

    @GetMapping("/filtrar/ano-mes")
    public ResponseEntity<List<Ticket>> filtrarPorAnoEMes(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(ticketService.findByYearAndMonth(year, month));
    }

    @PostMapping
    public ResponseEntity<Ticket> criar(@RequestBody TicketDTO dto) {
        return ResponseEntity.status(201).body(ticketService.criar(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        ticketService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
