package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    List<ContaBancaria> findByUsuarioId(Long usuarioId);
}
