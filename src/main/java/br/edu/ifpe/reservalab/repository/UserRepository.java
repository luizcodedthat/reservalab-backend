package br.edu.ifpe.reservalab.repository;

import br.edu.ifpe.reservalab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}