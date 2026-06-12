package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.SimilarTicket;

import java.util.List;

public record TicketAnalysisResponse(
        List<SimilarTicket> openDuplicates, // chamados OPEN/IN_PROGRESS similares
        String suggestion,                  // passo a passo gerado pela IA (pode ser null)
        boolean canProceed                  // frontend usa para decidir se mostra o form de criação
) {}