package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.*;
import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.model.Computer;
import br.edu.ifpe.reservalab.model.Laboratory;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.ComputerRepository;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import br.edu.ifpe.reservalab.repository.UserRepository;
import br.edu.ifpe.reservalab.specification.TicketSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final UserRepository userRepository;
    private final ComputerRepository computerRepository;
    private final TicketClassificationService classificationService;

    public Page<TicketResponse> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(TicketResponse::from);
    }

    public Page<TicketResponse> findAllByFilter(TicketFilter filter, Pageable pageable) {
        return ticketRepository.findAll(TicketSpecification.withFilter(filter), pageable)
                .map(TicketResponse::from);
    }

    public TicketResponse findById(Long id) {
        return TicketResponse.from(getOrThrow(id));
    }

    @Transactional
    public TicketResponse create(TicketRequest request) {
        Laboratory lab = laboratoryRepository.findById(request.laboratoryId())
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado."));

        User createdBy = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        Computer computer = request.computerId() != null
                ? computerRepository.findById(request.computerId())
                .orElseThrow(() -> new EntityNotFoundException("Computador não encontrado."))
                : null;

        // Classificação via IA (já aplica boost de cargo internamente)
        TicketClassificationResponse classification = classificationService.classify(
                new TicketClassificationRequest(request.title(), request.description(), request.userRole())
        );

        Ticket ticket = Ticket.builder()
                .laboratory(lab)
                .createdBy(createdBy)
                .computer(computer)
                .title(request.title())
                .description(request.description())
                .priority(classification.priority())
                .prazoResolucao(calcularPrazo(classification.priority()))
                .build();

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse update(Long id, TicketRequest request) {
        Ticket ticket = getOrThrow(id);
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setStatus(request.status());
        ticket.setPriority(request.priority());
        ticket.setResolutionComment(request.resolutionComment());
        if (request.status() == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    // Prazo calculado com base na prioridade definida pela IA
    private LocalDateTime calcularPrazo(Ticket.Priority priority) {
        return switch (priority) {
            case URGENT -> LocalDateTime.now().plusHours(1);
            case HIGH   -> LocalDateTime.now().plusHours(4);
            case MEDIUM -> LocalDateTime.now().plusHours(24);
            case LOW    -> LocalDateTime.now().plusHours(72);
        };
    }

    private Ticket getOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado."));
    }
}