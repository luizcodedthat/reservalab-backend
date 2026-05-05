package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.Laboratory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {

    Optional<Laboratory> findByCode(String code);

    boolean existsByCode(String code);

    
}