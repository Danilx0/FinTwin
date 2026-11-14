package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.HistoricoRemapeamentoIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoRemapeamentoIARepository extends JpaRepository<HistoricoRemapeamentoIA, Long> {
    List<HistoricoRemapeamentoIA> findByUsuarioId(Long usuarioId);

    long countByUsuarioId(Long usuarioId);
}
