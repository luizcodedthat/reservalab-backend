package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.LaboratoryRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import br.edu.ifpe.reservalab.model.Laboratory;
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

    @Override
    @Transactional
    public LaboratoryResponse create(LaboratoryRequest request) {
        if (laboratoryRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Código já cadastrado: " + request.code());
        }

        Laboratory lab = Laboratory.builder()
                .name(request.name())
                .code(request.code().toUpperCase())
                .description(request.description())
                .capacity(request.capacity())
                .computerCount(request.computerCount() != null ? request.computerCount() : 0)
                .building(request.building())
                .floor(request.floor())
                .active(request.active() != null ? request.active() : true)
                .build();

        return LaboratoryResponse.from(laboratoryRepository.save(lab));
    }

    @Override
    @Transactional
    public LaboratoryResponse update(Long id, LaboratoryRequest request) {
        Laboratory lab = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado: id=" + id));

        if (!lab.getCode().equals(request.code()) &&
                laboratoryRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Código já cadastrado: " + request.code());
        }

        lab.setName(request.name());
        lab.setCode(request.code().toUpperCase());
        lab.setDescription(request.description());
        lab.setCapacity(request.capacity());
        lab.setComputerCount(request.computerCount() != null ? request.computerCount() : 0);
        lab.setBuilding(request.building());
        lab.setFloor(request.floor());
        if (request.active() != null)
            lab.setActive(request.active());

        return LaboratoryResponse.from(laboratoryRepository.save(lab));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!laboratoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Laboratório não encontrado: id=" + id);
        }
        laboratoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        Laboratory lab = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado: id=" + id));
        lab.setActive(true);
        laboratoryRepository.save(lab);
        log.debug("Laboratório ativado: id={}", id);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Laboratory lab = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado: id=" + id));
        lab.setActive(false);
        laboratoryRepository.save(lab);
        log.debug("Laboratório desativado: id={}", id);
    }
}