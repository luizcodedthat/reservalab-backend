package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.LaboratoryResponse;
import br.edu.ifpe.reservalab.repository.LaboratoryRepository;
import br.edu.ifpe.reservalab.dto.CommentVoteSummary;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import br.edu.ifpe.reservalab.model.LaboratoryComment;
import br.edu.ifpe.reservalab.enums.VoteType;
import br.edu.ifpe.reservalab.repository.CommentVoteRepository;
import br.edu.ifpe.reservalab.repository.LaboratoryCommentRepository;
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
public class LaboratoryServiceImpl implements LaboratoryService {

    private final LaboratoryRepository        laboratoryRepository;
    private final LaboratoryCommentRepository commentRepository;
    private final CommentVoteRepository       voteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LaboratoryResponse> findAll(Pageable pageable) {
        log.debug("Listando laboratórios – page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return laboratoryRepository.findAll(pageable)
                .map(LaboratoryResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryResponse findById(Long id) {
        return laboratoryRepository.findById(id)
                .map(LaboratoryResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado: id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LaboratoryCommentResponse> findComments(Long laboratoryId, Pageable pageable) {
        if (!laboratoryRepository.existsById(laboratoryId)) {
            throw new EntityNotFoundException("Laboratório não encontrado: id=" + laboratoryId);
        }

        Page<LaboratoryComment> comments = commentRepository
                .findActiveByLaboratoryId(laboratoryId, pageable);

        List<Long> commentIds = comments.stream()
                .map(LaboratoryComment::getId)
                .toList();

        Map<Long, Long> upvotes   = countVotesByType(commentIds, VoteType.UPVOTE);
        Map<Long, Long> downvotes = countVotesByType(commentIds, VoteType.DOWNVOTE);

        return comments.map(comment -> LaboratoryCommentResponse.from(
                comment,
                upvotes.getOrDefault(comment.getId(), 0L),
                downvotes.getOrDefault(comment.getId(), 0L)
        ));
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