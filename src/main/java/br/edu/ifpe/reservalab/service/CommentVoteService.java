package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.enums.VoteType;

public interface CommentVoteService {

    void vote(Long commentId, Long userId, VoteType type);

    void removeVote(Long commentId, Long userId);
}