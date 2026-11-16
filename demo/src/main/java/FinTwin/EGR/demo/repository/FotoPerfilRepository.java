package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.FotoPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoPerfilRepository extends JpaRepository<FotoPerfil, Long> {
}
