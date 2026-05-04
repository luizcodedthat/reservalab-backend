package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LaboratoryResponse> findAll(Pageable pageable) {
        log.debug("Listando laboratórios – page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return laboratoryRepository.findAll(pageable)
                .map(LaboratoryResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryResponse findById(Long id) {
        return laboratoryRepository.findById(id)
                .map(LaboratoryResponse::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Laboratório não encontrado: id=" + id));
    }
}