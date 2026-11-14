package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.RelatorioExportacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioExportacaoRepository extends JpaRepository<RelatorioExportacao, Long> {
    List<RelatorioExportacao> findByUsuarioId(Long usuarioId);
}
