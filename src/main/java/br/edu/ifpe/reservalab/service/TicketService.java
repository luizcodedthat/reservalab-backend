package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.TicketDTO;
import br.edu.ifpe.reservalab.model.Laboratory;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.model.Ticket.Status;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import br.edu.ifpe.reservalab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private LaboratoryRepository laboratoryRepository;
    @Autowired private UserRepository userRepository;

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    public List<Ticket> filtrarPorStatus(Status status) {
        return ticketRepository.findByStatus(status);
    }

    public List<Ticket> filtrarPorLaboratorio(Long labId) {
        return ticketRepository.findByLaboratoryId(labId);
    }

    public List<Ticket> findByYear(int year) {
    return ticketRepository.findByYear(year); 
    }

    public List<Ticket> findByYearAndMonth(int year, int month) {
    return ticketRepository.findByYearAndMonth(year, month); 
    }

    public Ticket criar(TicketDTO dto) {
        Laboratory lab = laboratoryRepository.findById(dto.getLaboratoryId())
                .orElseThrow(() -> new RuntimeException("Laboratório não encontrado"));

        User createdBy = userRepository.findById(dto.getCreatedByUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Ticket ticket = new Ticket();
        ticket.setLaboratory(lab);
        ticket.setCreatedBy(createdBy);
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.OPEN);
        ticket.setPriority(dto.getPriority() != null ? dto.getPriority() : Ticket.Priority.MEDIUM);

        if (dto.getAssignedToUserId() != null) {
            User assignedTo = userRepository.findById(dto.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Usuário atribuído não encontrado"));
            ticket.setAssignedTo(assignedTo);
        }

        return ticketRepository.save(ticket);
    }

    public void deletar(Long id) {
        ticketRepository.deleteById(id);
    }
}
