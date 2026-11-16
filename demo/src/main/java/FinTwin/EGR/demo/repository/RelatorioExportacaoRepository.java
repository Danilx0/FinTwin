package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.RelatorioExportacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioExportacaoRepository extends JpaRepository<RelatorioExportacao, Long> {
}
