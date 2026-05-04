package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.dto.CommentVoteSummary;
import br.edu.ifpe.reservalab.dto.UserCommentVote;
import br.edu.ifpe.reservalab.model.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

    @Query("""
            SELECT new br.edu.ifpe.reservalab.dto.CommentVoteSummary(
                cv.comment.id,
                cv.voteType,
                COUNT(cv)
            )
            FROM CommentVote cv
            WHERE cv.comment.id IN :commentIds
            GROUP BY cv.comment.id, cv.voteType
            """)
    List<CommentVoteSummary> findVoteSummariesByCommentIds(
            @Param("commentIds") List<Long> commentIds
    );

    @Query("""
    SELECT cv
    FROM CommentVote cv
    WHERE cv.user.id = :userId
      AND cv.comment.id = :commentId
""")
    Optional<CommentVote> findVote(
            @Param("userId") Long userId,
            @Param("commentId") Long commentId
    );

    @Query("""
        SELECT new br.edu.ifpe.reservalab.dto.UserCommentVote(
            cv.comment.id,
            cv.voteType
        )
        FROM CommentVote cv
        WHERE cv.user.id = :userId
          AND cv.comment.id IN :commentIds
        """)
    List<UserCommentVote> findVotesByUserAndCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") List<Long> commentIds
    );

}