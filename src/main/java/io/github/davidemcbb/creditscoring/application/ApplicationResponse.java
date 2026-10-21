package io.github.davidemcbb.creditscoring.application;

import io.github.davidemcbb.creditscoring.scoring.Characteristic;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A scored application. {@code reasons} lists up to three characteristics that lower the
 * score most compared with the average applicant of the development sample.
 */
public record ApplicationResponse(
        UUID id,
        Instant createdAt,
        String scorecardVersion,
        ApplicationRequest input,
        int score,
        BigDecimal probabilityOfDefault,
        Decision decision,
        List<Reason> reasons,
        List<Points> points) {

    public record Reason(String characteristic, BigDecimal pointsBelowAverage) {
    }

    public record Points(String characteristic, String value, String attribute, int points) {
    }

    static ApplicationResponse of(CreditApplicationEntity e) {
        ApplicationRequest input = new ApplicationRequest(e.getDurationMonths(), e.getAmount(), e.getAge(),
                e.getEmploymentDuration(), e.getInstallmentRate(), e.getCheckingAccount(),
                e.getCreditHistory(), e.getPurpose(), e.getSavings(), e.getOtherDebtors());
        List<Reason> reasons = e.getReasons().stream()
                .map(r -> new Reason(r.getCharacteristic(), r.getPointsBelowAverage()))
                .toList();
        List<Points> points = e.getPoints().stream()
                .map(p -> new Points(p.getCharacteristic(), displayValue(p), p.getAttributeLabel(), p.getPoints()))
                .toList();
        return new ApplicationResponse(e.getPublicId(), e.getCreatedAt(), e.getScorecard().getVersion(), input,
                e.getScore(), e.getProbabilityOfDefault(), e.getDecision(), reasons, points);
    }

    private static String displayValue(CreditApplicationEntity.PointsLine p) {
        Characteristic c = Characteristic.fromKey(p.getCharacteristic());
        return c.categories().length == 0 ? String.valueOf(p.getInputValue()) : c.categoryName(p.getInputValue());
    }
}
