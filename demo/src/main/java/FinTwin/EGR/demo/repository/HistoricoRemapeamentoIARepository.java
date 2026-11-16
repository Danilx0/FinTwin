package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.HistoricoRemapeamentoIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoRemapeamentoIARepository extends JpaRepository<HistoricoRemapeamentoIA, Long> {

    long countByUsuarioId(Long usuarioId);
}
