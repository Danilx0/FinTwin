package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Usuario> findByTokenRecuperacaoSenha(String token);

    List<Usuario> findBySolicitouEsquecimentoTrueAndDataLimiteExclusaoBefore(LocalDateTime agora);
}
