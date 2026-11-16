package FinTwin.EGR.demo.repository;

import FinTwin.EGR.demo.model.CenarioEstresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CenarioEstresseRepository extends JpaRepository<CenarioEstresse, Long> {
}
