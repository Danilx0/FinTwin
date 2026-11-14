package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.GemeoDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GemeoDigitalRepository extends JpaRepository<GemeoDigital, Long> {
    List<GemeoDigital> findByUsuarioId(Long usuarioId);
}
