package io.github.davidemcbb.creditscoring.scorecard;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScorecardRepository extends JpaRepository<ScorecardEntity, Long> {

    Optional<ScorecardEntity> findByActiveTrue();
}
