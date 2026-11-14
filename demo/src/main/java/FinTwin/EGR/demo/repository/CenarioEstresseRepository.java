package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.CenarioEstresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CenarioEstresseRepository extends JpaRepository<CenarioEstresse, Long> {
    List<CenarioEstresse> findByUsuarioId(Long usuarioId);
}
