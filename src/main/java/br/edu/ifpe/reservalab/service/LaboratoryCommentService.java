package br.edu.ifpe.reservalab.service;

import br.edu.ifpe.reservalab.dto.LaboratoryCommentRequest;
import br.edu.ifpe.reservalab.dto.LaboratoryCommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LaboratoryCommentService {

    Page<LaboratoryCommentResponse> findByLaboratory(Long laboratoryId, Pageable pageable, Long userId);

    LaboratoryCommentResponse create(Long laboratoryId, LaboratoryCommentRequest request);

    void delete(Long laboratoryId, Long commentId);
}
