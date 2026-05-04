package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.dto.CommentVoteSummary;
import br.edu.ifpe.reservalab.model.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}