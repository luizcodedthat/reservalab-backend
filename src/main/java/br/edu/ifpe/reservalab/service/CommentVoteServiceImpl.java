package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.enums.VoteType;
import br.edu.ifpe.reservalab.model.CommentVote;
import br.edu.ifpe.reservalab.model.LaboratoryComment;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.CommentVoteRepository;
import br.edu.ifpe.reservalab.repository.LaboratoryCommentRepository;
import br.edu.ifpe.reservalab.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentVoteServiceImpl implements CommentVoteService {

    private final CommentVoteRepository voteRepository;
    private final LaboratoryCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void vote(Long commentId, Long userId, VoteType type) {

        LaboratoryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comentário não encontrado: id=" + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado: id=" + userId));

        voteRepository.findVote(userId, commentId)
                .ifPresentOrElse(existingVote -> {

                    if (existingVote.getVoteType() == type) {
                        voteRepository.delete(existingVote);

                        log.info("Voto removido: comment={}, user={}",
                                commentId, userId);
                        return;
                    }

                    existingVote.setVoteType(type);

                    log.info("Voto atualizado: comment={}, user={}, type={}",
                            commentId, userId, type);

                }, () -> {
                    // ➕ novo voto
                    CommentVote vote = CommentVote.builder()
                            .comment(comment)
                            .user(user)
                            .voteType(type)
                            .build();

                    voteRepository.save(vote);

                    log.info("Voto criado: comment={}, user={}, type={}",
                            commentId, userId, type);
                });
    }

    @Override
    @Transactional
    public void removeVote(Long commentId, Long userId) {
        voteRepository.findVote(userId, commentId)
                .ifPresent(vote -> {
                    voteRepository.delete(vote);
                    log.info("Voto removido explicitamente: comment={}, user={}",
                            commentId, userId);
                });
    }
}