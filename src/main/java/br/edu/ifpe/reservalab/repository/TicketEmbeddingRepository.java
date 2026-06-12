package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.enums.TicketStatus;
import br.edu.ifpe.reservalab.model.SimilarTicket;
import br.edu.ifpe.reservalab.model.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TicketEmbeddingRepository {

    private final JdbcTemplate jdbc;

    public void save(Long ticketId, float[] embedding) {
        jdbc.update(
                """
                INSERT INTO ticket_embeddings (ticket_id, embedding)
                VALUES (?, ?::vector)
                ON CONFLICT (ticket_id) DO UPDATE SET embedding = EXCLUDED.embedding
                """,
                ticketId, toVectorString(embedding)
        );
    }

    public List<SimilarTicket> findSimilar(float[] embedding, double threshold, int limit) {
        String vec = toVectorString(embedding);
        return jdbc.query(
                """
                SELECT t.id, t.title, t.description, t.status, t.priority,
                       1 - (te.embedding <=> ?::vector) AS similarity
                FROM ticket_embeddings te
                JOIN tickets t ON t.id = te.ticket_id
                WHERE 1 - (te.embedding <=> ?::vector) > ?
                ORDER BY te.embedding <=> ?::vector
                LIMIT ?
                """,
                (rs, i) -> new SimilarTicket(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        TicketStatus.valueOf(rs.getString("status")),
                        Ticket.Priority.valueOf(rs.getString("priority")),
                        rs.getDouble("similarity")
                ),
                vec, vec, threshold, vec, limit
        );
    }

    // "[0.123, 0.456, ...]" — formato aceito pelo pgvector
    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        return sb.append("]").toString();
    }
}