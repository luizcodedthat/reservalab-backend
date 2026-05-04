package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LaboratoryService {

    Page<LaboratoryResponse> findAll(Pageable pageable);

    LaboratoryResponse findById(Long id);
}