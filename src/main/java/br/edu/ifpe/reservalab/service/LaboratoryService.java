package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.LaboratoryRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LaboratoryService {

    Page<LaboratoryResponse> findAll(Pageable pageable);

    LaboratoryResponse findById(Long id);

    // Adicionar à interface existente
    LaboratoryResponse create(LaboratoryRequest request);

    LaboratoryResponse update(Long id, LaboratoryRequest request);

    void delete(Long id); // já existe 

    void activate(Long id); // reativa

    void deactivate(Long id); // desativa
}