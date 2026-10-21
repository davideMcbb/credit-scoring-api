package io.github.davidemcbb.creditscoring.application;

import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditApplicationRepository extends JpaRepository<CreditApplicationEntity, Long> {

    Optional<CreditApplicationEntity> findByPublicId(UUID publicId);

    Page<CreditApplicationEntity> findByDecision(Decision decision, Pageable pageable);
}
