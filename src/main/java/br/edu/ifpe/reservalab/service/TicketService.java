package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.TicketDTO;
import br.edu.ifpe.reservalab.dto.TicketFilter;
import br.edu.ifpe.reservalab.dto.TicketResponse;
import br.edu.ifpe.reservalab.dto.TicketClassificationRequest; 
import br.edu.ifpe.reservalab.dto.TicketClassificationResponse; 
import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import br.edu.ifpe.reservalab.specification.TicketSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketClassificationService classificationService; // Injeção do serviço de IA

    public Page<TicketResponse> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(TicketResponse::from);
    }

    public Page<TicketResponse> findAllByFilter(TicketFilter filter, Pageable pageable) {
        return ticketRepository.findAll(TicketSpecification.withFilter(filter), pageable)
                .map(TicketResponse::from);
    }

    public TicketResponse findById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse create(TicketDTO dto) {
        
        TicketClassificationResponse aiClassification = classificationService.classify(
                new TicketClassificationRequest(dto.getTitle(), dto.getDescription())
        );

        Ticket ticket = new Ticket();
        ticket.setLaboratoryId(dto.getLaboratoryId());
        ticket.setCreatedByUserId(dto.getCreatedByUserId());
        ticket.setAssignedToUserId(dto.getAssignedToUserId());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        
       
        ticket.setStatus(dto.getStatus() != null ? dto.getStatus() : TicketStatus.OPEN);
        
        
        ticket.setPriority(aiClassification.priority());
        
        ticket.setResolutionComment(dto.getResolutionComment());

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    public void delete(Long id) {
        ticketRepository.deleteById(id);
    }
    
    @Transactional
    public TicketResponse update(Long id, TicketDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket não encontrado"));

        ticket.setStatus(dto.getStatus());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setResolutionComment(dto.getResolutionComment());
        ticket.setLaboratoryId(dto.getLaboratoryId());

        return TicketResponse.from(ticketRepository.save(ticket));
    }
}