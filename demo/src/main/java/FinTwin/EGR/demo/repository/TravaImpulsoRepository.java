package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.TravaImpulso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravaImpulsoRepository extends JpaRepository<TravaImpulso, Long> {
    List<TravaImpulso> findByUsuarioIdOrderByDataEntradaQuarentenaDesc(Long usuarioId);
}
