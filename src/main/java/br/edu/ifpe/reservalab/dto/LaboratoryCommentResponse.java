package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.model.LaboratoryComment;

import java.time.LocalDateTime;

public record LaboratoryCommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String content,
        long upvotes,
        long downvotes,
        LocalDateTime editedAt,
        LocalDateTime createdAt
) {
    public static LaboratoryCommentResponse from(LaboratoryComment comment,
                                                 long upvotes,
                                                 long downvotes) {
        return new LaboratoryCommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getContent(),
                upvotes,
                downvotes,
                comment.getEditedAt(),
                comment.getCreatedAt()
        );
    }
}