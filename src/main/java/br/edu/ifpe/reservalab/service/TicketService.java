package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.TicketClassificationRequest;
import br.edu.ifpe.reservalab.dto.TicketClassificationResponse;
import br.edu.ifpe.reservalab.dto.TicketDTO;
import br.edu.ifpe.reservalab.dto.TicketFilter;
import br.edu.ifpe.reservalab.dto.TicketResponse;
import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.exception.ForbiddenOperationException;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import br.edu.ifpe.reservalab.specification.TicketSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final TicketClassificationService classificationService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<TicketResponse> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> findAllByFilter(TicketFilter filter, Pageable pageable) {
        return ticketRepository.findAll(TicketSpecification.withFilter(filter), pageable)
                .map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {
        return TicketResponse.from(getOrThrow(id));
    }

    /**
     * Cria um chamado em nome do usuário autenticado.
     * A prioridade é sugerida pela IA considerando o cargo do solicitante;
     * se a IA falhar, cai para MEDIUM (ver TicketClassificationService).
     */
    @Transactional
    public TicketResponse create(TicketDTO dto) {
        if (dto.getLaboratoryId() == null || isBlank(dto.getTitle()) || isBlank(dto.getDescription())) {
            throw new IllegalArgumentException("laboratoryId, title e description são obrigatórios.");
        }
        if (!laboratoryRepository.existsById(dto.getLaboratoryId())) {
            throw new EntityNotFoundException("Laboratório não encontrado: id=" + dto.getLaboratoryId());
        }

        User creator = userService.getCurrentUser();
        boolean staff = UserService.isStaff(creator);

        TicketClassificationResponse aiClassification = classificationService.classify(
                new TicketClassificationRequest(dto.getTitle(), dto.getDescription(), creator.getRole().name())
        );

        Ticket ticket = new Ticket();
        ticket.setLaboratoryId(dto.getLaboratoryId());
        ticket.setCreatedByUserId(creator.getId());
        ticket.setAssignedToUserId(staff ? dto.getAssignedToUserId() : null);
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(staff && dto.getStatus() != null ? dto.getStatus() : TicketStatus.OPEN);
        ticket.setPriority(aiClassification.priority() != null ? aiClassification.priority() : Ticket.Priority.MEDIUM);

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    /**
     * Atualização parcial: campos nulos são mantidos.
     * O criador pode editar título/descrição; status, prioridade, responsável
     * e comentário de resolução são exclusivos de SECRETARY/TECHNICIAN.
     */
    @Transactional
    public TicketResponse update(Long id, TicketDTO dto) {
        Ticket ticket = getOrThrow(id);
        User currentUser = userService.getCurrentUser();
        boolean staff = UserService.isStaff(currentUser);

        if (!staff && !ticket.getCreatedByUserId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Este chamado pertence a outro usuário.");
        }

        if (!isBlank(dto.getTitle()))       ticket.setTitle(dto.getTitle());
        if (!isBlank(dto.getDescription())) ticket.setDescription(dto.getDescription());

        if (dto.getLaboratoryId() != null && !dto.getLaboratoryId().equals(ticket.getLaboratoryId())) {
            if (!laboratoryRepository.existsById(dto.getLaboratoryId())) {
                throw new EntityNotFoundException("Laboratório não encontrado: id=" + dto.getLaboratoryId());
            }
            ticket.setLaboratoryId(dto.getLaboratoryId());
        }

        boolean changesStaffFields = dto.getStatus() != null || dto.getPriority() != null
                || dto.getAssignedToUserId() != null || dto.getResolutionComment() != null;

        if (changesStaffFields && !staff) {
            throw new ForbiddenOperationException(
                    "Apenas secretaria ou técnicos alteram status, prioridade, responsável ou resolução.");
        }

        if (dto.getPriority() != null)          ticket.setPriority(dto.getPriority());
        if (dto.getAssignedToUserId() != null)  ticket.setAssignedToUserId(dto.getAssignedToUserId());
        if (dto.getResolutionComment() != null) ticket.setResolutionComment(dto.getResolutionComment());

        if (dto.getStatus() != null && dto.getStatus() != ticket.getStatus()) {
            ticket.setStatus(dto.getStatus());
            boolean finished = dto.getStatus() == TicketStatus.RESOLVED || dto.getStatus() == TicketStatus.CLOSED;
            ticket.setResolvedAt(finished ? LocalDateTime.now() : null);
        }

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(Long id) {
        ticketRepository.delete(getOrThrow(id));
    }

    private Ticket getOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado: id=" + id));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
