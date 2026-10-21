package io.github.davidemcbb.creditscoring.scoring;

import static io.github.davidemcbb.creditscoring.scoring.Characteristic.AGE;
import static io.github.davidemcbb.creditscoring.scoring.Characteristic.DURATION;
import static io.github.davidemcbb.creditscoring.scoring.Characteristic.OTHER_DEBTORS;
import static io.github.davidemcbb.creditscoring.scoring.Characteristic.PURPOSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.github.davidemcbb.creditscoring.scoring.Inputs.CheckingAccount;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CreditHistory;
import io.github.davidemcbb.creditscoring.scoring.Inputs.EmploymentDuration;
import io.github.davidemcbb.creditscoring.scoring.Inputs.InstallmentRate;
import io.github.davidemcbb.creditscoring.scoring.Inputs.OtherDebtors;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Purpose;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Savings;
import io.github.davidemcbb.creditscoring.scoring.Rule.Bin;
import io.github.davidemcbb.creditscoring.scoring.Rule.Kind;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Line;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Reason;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ScorecardTest {

    // 20 points double the odds; 600 points mean odds of 50:1
    static final double FACTOR = 20 / Math.log(2);
    static final double OFFSET = 600 - FACTOR * Math.log(50);

    static final List<Rule> RULES = List.of(
            new Rule(DURATION, Kind.NUMERIC, 50, 65, List.of(
                    Bin.upTo("<= 12", BigDecimal.valueOf(12), 60),
                    Bin.upTo("12 < x <= 24", BigDecimal.valueOf(24), 50),
                    Bin.upTo("> 24", null, 40))),
            new Rule(AGE, Kind.NUMERIC, 50, 50, List.of(
                    Bin.upTo("<= 30", BigDecimal.valueOf(30), 40),
                    Bin.upTo("> 30", null, 60))),
            new Rule(PURPOSE, Kind.CATEGORICAL, 55, 55, List.of(
                    Bin.ofCodes("others / new car", Set.of(0, 1), 70),
                    Bin.ofCodes("used car", Set.of(2), 40))),
            new Rule(OTHER_DEBTORS, Kind.CATEGORICAL, 50, 60, List.of(
                    Bin.ofCodes("none / co-applicant", Set.of(1, 2), 50),
                    Bin.ofCodes("guarantor", Set.of(3), 80))));

    static Scorecard scorecard(double cutoff) {
        return new Scorecard(1, "test", FACTOR, OFFSET, cutoff, RULES);
    }

    static Applicant applicant(int duration, int age, Purpose purpose, OtherDebtors debtors) {
        return new Applicant(duration, 1000, age, EmploymentDuration.FROM_1_TO_4_YEARS,
                InstallmentRate.UNDER_20_PERCENT, CheckingAccount.NO_CHECKING_ACCOUNT,
                CreditHistory.EXISTING_PAID_DULY, purpose, Savings.NONE_OR_UNKNOWN, debtors);
    }

    @Test
    void noReasonForCharacteristicsAtOrAboveAverage() {
        // 70 points for a new car against an average of 55, 80 for a guarantor against 60
        ScoreResult r = scorecard(0).score(applicant(12, 40, Purpose.NEW_CAR, OtherDebtors.GUARANTOR));
        assertThat(r.reasons()).extracting(Reason::characteristic).containsExactly(DURATION);
    }

    @Test
    void upperBoundsAreInclusive() {
        Scorecard s = scorecard(0);
        assertThat(points(s.score(applicant(12, 40, Purpose.OTHERS, OtherDebtors.NONE)), DURATION)).isEqualTo(60);
        assertThat(points(s.score(applicant(13, 40, Purpose.OTHERS, OtherDebtors.NONE)), DURATION)).isEqualTo(50);
        assertThat(points(s.score(applicant(24, 40, Purpose.OTHERS, OtherDebtors.NONE)), DURATION)).isEqualTo(50);
        assertThat(points(s.score(applicant(72, 40, Purpose.OTHERS, OtherDebtors.NONE)), DURATION)).isEqualTo(40);
    }

    @Test
    void codesInTheSameClassGetTheSamePoints() {
        Scorecard s = scorecard(0);
        ScoreResult none = s.score(applicant(12, 40, Purpose.OTHERS, OtherDebtors.NONE));
        ScoreResult coApplicant = s.score(applicant(12, 40, Purpose.OTHERS, OtherDebtors.CO_APPLICANT));
        assertThat(points(none, OTHER_DEBTORS)).isEqualTo(50).isEqualTo(points(coApplicant, OTHER_DEBTORS));
    }

    @Test
    void codeNeverSeenGetsNeutralPoints() {
        ScoreResult r = scorecard(0).score(applicant(12, 40, Purpose.EDUCATION, OtherDebtors.NONE));
        Line purpose = line(r, PURPOSE);
        assertThat(purpose.points()).isEqualTo(55);
        assertThat(purpose.attribute()).isEqualTo(Scorecard.NOT_SEEN);
    }

    @Test
    void scoreIsTheSumOfThePoints() {
        ScoreResult r = scorecard(0).score(applicant(12, 25, Purpose.USED_CAR, OtherDebtors.NONE));
        assertThat(r.lines()).extracting(Line::characteristic).containsExactly(DURATION, AGE, PURPOSE, OTHER_DEBTORS);
        assertThat(r.lines()).extracting(Line::points).containsExactly(60, 40, 40, 50);
        assertThat(r.score()).isEqualTo(190);
    }

    @Test
    void probabilityFollowsTheScaling() {
        Scorecard s = scorecard(0);
        assertThat(s.probabilityOfDefault(600)).isCloseTo(1.0 / 51, within(1e-12));   // odds 50:1
        assertThat(s.probabilityOfDefault(620)).isCloseTo(1.0 / 101, within(1e-12));  // odds doubled
        assertThat(s.probabilityOfDefault((int) Math.round(OFFSET))).isCloseTo(0.5, within(0.01));
    }

    @Test
    void approvedAtOrAboveTheCutoff() {
        Applicant a = applicant(12, 25, Purpose.USED_CAR, OtherDebtors.NONE);   // 190 points
        assertThat(scorecard(190).score(a).decision()).isEqualTo(Decision.APPROVE);
        assertThat(scorecard(190.5).score(a).decision()).isEqualTo(Decision.REJECT);
    }

    @Test
    void reasonsAreTheThreeLargestGapsBelowAverage() {
        // gaps against the average: duration 5, age 10, purpose 15, other debtors 10
        ScoreResult r = scorecard(0).score(applicant(12, 25, Purpose.USED_CAR, OtherDebtors.NONE));
        assertThat(r.reasons()).extracting(Reason::characteristic)
                .containsExactly(PURPOSE, AGE, OTHER_DEBTORS);   // ties keep the scorecard order
        assertThat(r.reasons().get(0).pointsBelowAverage()).isCloseTo(15, within(1e-9));
    }

    private static Line line(ScoreResult r, Characteristic c) {
        return r.lines().stream().filter(l -> l.characteristic() == c).findFirst().orElseThrow();
    }

    private static int points(ScoreResult r, Characteristic c) {
        return line(r, c).points();
    }
}
