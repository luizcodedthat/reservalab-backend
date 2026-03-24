package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.ReservationFilter;
import br.edu.ifpe.reservalab.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {

    Page<ReservationResponse> findAll(Pageable pageable);

    Page<ReservationResponse> findAllByFilter(ReservationFilter filter, Pageable pageable);

    ReservationResponse findById(Long id);

    void cancel(Long id);

    int cancelByFilter(ReservationFilter filter);
<<<<<<< HEAD
}
=======
}
>>>>>>> c57f8e7 (feat: implementa busca e delecao parametrizadas)
