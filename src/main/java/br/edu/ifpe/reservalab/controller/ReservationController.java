package br.edu.ifpe.reservalab.controller;

import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.dto.ReservationRequest;
import br.edu.ifpe.reservalab.dto.ReservationResponse;
import br.edu.ifpe.reservalab.enums.ReservationStatus;
import br.edu.ifpe.reservalab.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid ReservationRequest request) {
        ReservationResponse response = reservationService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> listAll(
            @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.findAll(pageable));
    }

    @GetMapping("/today")
    public ResponseEntity<Page<ReservationResponse>> listToday(
            @PageableDefault(size = 20, sort = "laboratoryId") Pageable pageable
    ) {
        LocalDate today = LocalDate.now();
        ReservationFilter todayFilter = new ReservationFilter(
                null, null, null, today, today, null
        );
        return ResponseEntity.ok(reservationService.findAllByFilter(todayFilter, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ReservationResponse>> search(
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) Long requestedByUserId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long groupId,
            @PageableDefault(size = 20, sort = "reservationDate") Pageable pageable
    ) {

        LocalDate effectiveDateFrom = dateFrom != null ? dateFrom : LocalDate.now();
        LocalDate effectiveDateTo = dateTo != null ? dateTo : LocalDate.now();

        ReservationFilter filter = new ReservationFilter(
                laboratoryId, requestedByUserId, status, effectiveDateFrom, effectiveDateTo, groupId
        );
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
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) Long requestedByUserId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long groupId
    ) {
        ReservationFilter filter = new ReservationFilter(
                laboratoryId, requestedByUserId, null, dateFrom, dateTo, groupId
        );
        int cancelled = reservationService.cancelByFilter(filter);
        return ResponseEntity.ok(Map.of("cancelled", cancelled));
    }
}
