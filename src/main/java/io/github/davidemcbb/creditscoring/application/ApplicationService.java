package io.github.davidemcbb.creditscoring.application;

import io.github.davidemcbb.creditscoring.scorecard.ScorecardRepository;
import io.github.davidemcbb.creditscoring.scorecard.ScorecardService;
import io.github.davidemcbb.creditscoring.scoring.Applicant;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import io.github.davidemcbb.creditscoring.scoring.Scorecard;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

    private final ScorecardService scorecards;
    private final ScorecardRepository scorecardRepository;
    private final CreditApplicationRepository applications;
    private final Clock clock;

    public ApplicationService(ScorecardService scorecards, ScorecardRepository scorecardRepository,
                              CreditApplicationRepository applications, Clock clock) {
        this.scorecards = scorecards;
        this.scorecardRepository = scorecardRepository;
        this.applications = applications;
        this.clock = clock;
    }

    @Transactional
    public ApplicationResponse score(ApplicationRequest request) {
        Scorecard scorecard = scorecards.active();
        Applicant applicant = request.toApplicant();
        ScoreResult result = scorecard.score(applicant);
        // PostgreSQL keeps microseconds: truncating here makes the response match a later read
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        CreditApplicationEntity entity = CreditApplicationEntity.scored(applicant, result,
                scorecardRepository.getReferenceById(scorecard.id()), now);
        return ApplicationResponse.of(applications.save(entity));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(UUID id) {
        return applications.findByPublicId(id)
                .map(ApplicationResponse::of)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> list(Decision decision, Pageable pageable) {
        Page<CreditApplicationEntity> page = decision == null
                ? applications.findAll(pageable)
                : applications.findByDecision(decision, pageable);
        return page.map(ApplicationResponse::of);
    }
}
