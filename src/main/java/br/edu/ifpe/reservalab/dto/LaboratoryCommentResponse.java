package br.edu.ifpe.reservalab.dto;

import br.edu.ifpe.reservalab.enums.VoteType;
import br.edu.ifpe.reservalab.model.LaboratoryComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaboratoryCommentResponse {

    private Long id;
    private Long authorId;
    private String authorName;
    private String content;
    private Integer rating;
    private long upvotes;
    private long downvotes;
    private LocalDateTime editedAt;
    private LocalDateTime createdAt;

    private VoteType userVote;

    public static LaboratoryCommentResponse from(
            LaboratoryComment comment,
            long upvotes,
            long downvotes,
            VoteType userVote
    ) {
        return LaboratoryCommentResponse.builder()
                .id(comment.getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .content(comment.getContent())
                .upvotes(upvotes)
                .downvotes(downvotes)
                .editedAt(comment.getEditedAt())
                .createdAt(comment.getCreatedAt())
                .userVote(userVote)
                .build();
    }
}