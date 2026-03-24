package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.VoteType;

public record CommentVoteSummary(
        Long commentId,
        VoteType voteType,
        long count
) {}