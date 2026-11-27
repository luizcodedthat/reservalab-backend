package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.CommentVoteSummary;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import br.edu.ifpe.reservalab.dto.UserCommentVote;
import br.edu.ifpe.reservalab.enums.VoteType;
import br.edu.ifpe.reservalab.model.CommentVote;
import br.edu.ifpe.reservalab.model.Laboratory;
import br.edu.ifpe.reservalab.model.LaboratoryComment;
import br.edu.ifpe.reservalab.model.User;
import br.edu.ifpe.reservalab.repository.CommentVoteRepository;
import br.edu.ifpe.reservalab.repository.LaboratoryCommentRepository;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.exception.ForbiddenOperationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryCommentServiceImpl implements LaboratoryCommentService {

    private final LaboratoryCommentRepository commentRepository;
    private final CommentVoteRepository       voteRepository;
    private final LaboratoryRepository        laboratoryRepository;
    private final UserService                 userService;

    @Override
    @Transactional(readOnly = true)
    public Page<LaboratoryCommentResponse> findByLaboratory(Long laboratoryId, Pageable pageable, Long userId) {
        if (!laboratoryRepository.existsById(laboratoryId)) {
            throw new EntityNotFoundException("Laboratório não encontrado: id=" + laboratoryId);
        }

        Page<LaboratoryComment> comments = commentRepository.findActiveByLaboratoryId(laboratoryId, pageable);

        List<Long> commentIds = comments.stream()
                .map(LaboratoryComment::getId)
                .toList();

        // Contadores gerais de votos
        Map<Long, Long> upvotes = countVotesByType(commentIds, VoteType.UPVOTE);
        Map<Long, Long> downvotes = countVotesByType(commentIds, VoteType.DOWNVOTE);

        // Map do voto do usuário para cada comentário
        Map<Long, VoteType> userVotes = (userId == null || commentIds.isEmpty())
                ? Map.of()
                : voteRepository.findVotesByUserAndCommentIds(userId, commentIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UserCommentVote::commentId,
                                UserCommentVote::voteType
                        ));

        // Monta o DTO incluindo o voto do usuário
        return comments.map(comment -> LaboratoryCommentResponse.from(
                comment,
                upvotes.getOrDefault(comment.getId(), 0L),
                downvotes.getOrDefault(comment.getId(), 0L),
                userVotes.get(comment.getId()) // aqui entra o voto do usuário
        ));
    }

    @Override
    @Transactional
    public LaboratoryCommentResponse create(Long laboratoryId, LaboratoryCommentRequest request) {

        Laboratory laboratory = laboratoryRepository.findById(laboratoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Laboratório não encontrado: id=" + laboratoryId));

        User loggedUser = userService.getCurrentUser();

        LaboratoryComment comment = LaboratoryComment.builder()
                .laboratory(laboratory)
                .author(loggedUser)
                .content(request.content())
                .build();

        LaboratoryComment saved = commentRepository.save(comment);

        log.info("Comentário criado: id={}, lab={}, autor={}",
                saved.getId(), laboratoryId, loggedUser.getEmail());

        return LaboratoryCommentResponse.from(saved, 0L, 0L, null);
    }

    @Override
    @Transactional
    public void delete(Long laboratoryId, Long commentId) {
        LaboratoryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comentário não encontrado: id=" + commentId));

        if (comment.isDeleted() || !comment.getLaboratory().getId().equals(laboratoryId)) {
            throw new EntityNotFoundException(
                    "Comentário id=" + commentId + " não encontrado no laboratório id=" + laboratoryId);
        }

        User currentUser = userService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId()) && !UserService.isStaff(currentUser)) {
            throw new ForbiddenOperationException("Apenas o autor ou a equipe podem remover este comentário.");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);

        log.info("Comentário deletado (soft): id={}", commentId);
    }

    private Map<Long, Long> countVotesByType(List<Long> commentIds, VoteType type) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }

        return voteRepository.findVoteSummariesByCommentIds(commentIds)
                .stream()
                .filter(s -> s.voteType() == type)
                .collect(Collectors.toMap(
                        CommentVoteSummary::commentId,
                        CommentVoteSummary::count
                ));
    }
}