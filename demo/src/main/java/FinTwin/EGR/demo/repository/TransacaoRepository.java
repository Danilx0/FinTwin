package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.Transacao;
import FinTwin.EGR.demo.model.enums.CategoriaTransacao;
import FinTwin.EGR.demo.model.enums.TipoTransacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByUsuarioId(Long usuarioId);
    List<Transacao> findByUsuarioIdAndDataHoraBetween(Long usuarioId, LocalDateTime inicio, LocalDateTime fim);
    List<Transacao> findByUsuarioIdAndTipoTransacao(Long usuarioId, TipoTransacao tipo);
    List<Transacao> findByUsuarioIdAndCategoria(Long usuarioId, CategoriaTransacao categoria);

    List<Transacao> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId, Pageable pageable);
}
