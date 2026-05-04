package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.VoteType;

public record CommentVoteRequest(Long userId, VoteType type) {}
