package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.LaboratoryComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratoryCommentRepository extends JpaRepository<LaboratoryComment, Long> {

    @Query(value = """
            SELECT c FROM LaboratoryComment c
            JOIN FETCH c.author
            WHERE c.laboratory.id = :laboratoryId
              AND c.deleted = false
            """,
            countQuery = """
            SELECT COUNT(c) FROM LaboratoryComment c
            WHERE c.laboratory.id = :laboratoryId
              AND c.deleted = false
            """)
    Page<LaboratoryComment> findActiveByLaboratoryId(
            @Param("laboratoryId") Long laboratoryId,
            Pageable pageable
    );
}