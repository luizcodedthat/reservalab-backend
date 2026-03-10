package br.edu.ifpe.reservalab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @GetMapping
    public ResponseEntity getAll() {
        return ResponseEntity.status(HttpStatus.OK).body("Teste");
    }

}

