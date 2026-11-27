package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.VoteType;
import jakarta.validation.constraints.NotNull;

/** O votante é sempre o usuário autenticado. Repetir o mesmo voto remove-o (toggle). */
public record CommentVoteRequest(
        @NotNull(message = "O tipo do voto é obrigatório")
        VoteType type
) {}
