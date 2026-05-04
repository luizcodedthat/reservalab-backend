package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.dto.ReservationRequest;
import br.edu.ifpe.reservalab.dto.ReservationResponse;
import br.edu.ifpe.reservalab.dto.ReservationUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {

    ReservationResponse create(ReservationRequest request);

    Page<ReservationResponse> findAll(Pageable pageable);

    Page<ReservationResponse> findAllByFilter(ReservationFilter filter, Pageable pageable);

    ReservationResponse findById(Long id);

    void cancel(Long id);

    int cancelByFilter(ReservationFilter filter);
    
    ReservationResponse update(Long id, ReservationUpdateRequest request);
}
