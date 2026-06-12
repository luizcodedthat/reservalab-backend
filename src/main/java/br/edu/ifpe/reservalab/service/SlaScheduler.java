package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.model.Ticket;
import br.edu.ifpe.reservalab.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private static final List<TicketStatus> ATIVOS =
            List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.PENDING);

    private final TicketRepository ticketRepository;

    @Scheduled(fixedDelay = 15 * 60 * 1000) // a cada 15 minutos
    @Transactional
    public void verificarSlaVencido() {
        List<Ticket> vencidos = ticketRepository.findSlaVencidos(LocalDateTime.now(), ATIVOS);

        for (Ticket ticket : vencidos) {
            log.warn("SLA vencido — ticket #{} | prioridade {} | venceu em {}",
                    ticket.getId(), ticket.getPriority(), ticket.getPrazoResolucao());
            // Extensível: disparar evento de domínio, notificar via e-mail, etc.
        }

        if (!vencidos.isEmpty()) {
            log.info("Verificação SLA concluída: {} chamados vencidos.", vencidos.size());
        }
    }
}