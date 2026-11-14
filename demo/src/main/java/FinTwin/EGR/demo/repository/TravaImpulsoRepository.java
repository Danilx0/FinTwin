package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.TravaImpulso;
import FinTwin.EGR.demo.model.enums.StatusTravaImpulso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravaImpulsoRepository extends JpaRepository<TravaImpulso, Long> {
    List<TravaImpulso> findByUsuarioId(Long usuarioId);
    List<TravaImpulso> findByUsuarioIdAndStatus(Long usuarioId, StatusTravaImpulso status);
}
