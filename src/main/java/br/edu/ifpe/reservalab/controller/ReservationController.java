package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.dto.ReservationResponse;
import br.edu.ifpe.reservalab.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> listAll(
            @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ReservationResponse>> search(
            @ModelAttribute ReservationFilter filter,
            @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.findAllByFilter(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Integer>> cancelByFilter(
            @ModelAttribute ReservationFilter filter
    ) {
        int cancelled = reservationService.cancelByFilter(filter);
        return ResponseEntity.ok(Map.of("cancelled", cancelled));
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> c57f8e7 (feat: implementa busca e delecao parametrizadas)
