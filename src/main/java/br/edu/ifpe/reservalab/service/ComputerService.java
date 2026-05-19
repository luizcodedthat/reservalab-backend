package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.ComputerRequest;
import br.edu.ifpe.reservalab.dto.ComputerResponse;
import br.edu.ifpe.reservalab.model.Computer;
import br.edu.ifpe.reservalab.model.Laboratory;
import br.edu.ifpe.reservalab.repository.ComputerRepository;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComputerService {

    private final ComputerRepository computerRepository;
    private final LaboratoryRepository laboratoryRepository;

    public ComputerResponse findById(Long id) {
        return ComputerResponse.from(getOrThrow(id));
    }

    public ComputerResponse findByPatrimonio(String patrimonio) {
        return computerRepository.findByPatrimonio(patrimonio)
                .map(ComputerResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Computador não encontrado: " + patrimonio));
    }

    public List<ComputerResponse> findByLaboratory(Long laboratoryId) {
        return computerRepository.findByLaboratoryIdAndActiveTrue(laboratoryId)
                .stream().map(ComputerResponse::from).toList();
    }

    @Transactional
    public ComputerResponse create(ComputerRequest request) {
        Laboratory lab = laboratoryRepository.findById(request.laboratoryId())
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado."));

        Computer computer = Computer.builder()
                .patrimonio(request.patrimonio())
                .laboratory(lab)
                .ip(request.ip())
                .processador(request.processador())
                .ram(request.ram())
                .so(request.so())
                .build();

        return ComputerResponse.from(computerRepository.save(computer));
    }

    @Transactional
    public void deactivate(Long id) {
        Computer computer = getOrThrow(id);
        computer.setActive(false);
    }

    private Computer getOrThrow(Long id) {
        return computerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Computador não encontrado."));
    }
}