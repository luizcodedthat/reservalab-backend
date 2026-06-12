package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, Long> {
    Optional<Computer> findByPatrimonio(String patrimonio);
    List<Computer> findByLaboratoryIdAndActiveTrue(Long laboratoryId);
}
