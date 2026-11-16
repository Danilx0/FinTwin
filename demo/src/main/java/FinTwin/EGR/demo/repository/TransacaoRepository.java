package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.Transacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByUsuarioId(Long usuarioId);

    List<Transacao> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId, Pageable pageable);
}
