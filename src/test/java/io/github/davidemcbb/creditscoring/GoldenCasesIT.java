package io.github.davidemcbb.creditscoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.github.davidemcbb.creditscoring.scorecard.ScorecardService;
import io.github.davidemcbb.creditscoring.scoring.Applicant;
import io.github.davidemcbb.creditscoring.scoring.Characteristic;
import io.github.davidemcbb.creditscoring.scoring.Coded;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CheckingAccount;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CreditHistory;
import io.github.davidemcbb.creditscoring.scoring.Inputs.EmploymentDuration;
import io.github.davidemcbb.creditscoring.scoring.Inputs.InstallmentRate;
import io.github.davidemcbb.creditscoring.scoring.Inputs.OtherDebtors;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Purpose;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Savings;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * The scorecard read from the database must give, for each of the 1,000 loans, the same
 * score, probability of default, decision and reasons as the Python project that estimated it.
 * The expected values come from tools/export_scorecard.py.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GoldenCasesIT {

    @Autowired
    ScorecardService scorecards;

    @ParameterizedTest(name = "loan {0}")
    @CsvFileSource(resources = "/golden_cases.csv", numLinesToSkip = 1)
    void sameResultAsThePythonScorecard(ArgumentsAccessor row) {
        Applicant applicant = new Applicant(
                row.getInteger(1), row.getInteger(2), row.getInteger(3),
                Coded.fromCode(EmploymentDuration.class, row.getInteger(4)),
                Coded.fromCode(InstallmentRate.class, row.getInteger(5)),
                Coded.fromCode(CheckingAccount.class, row.getInteger(6)),
                Coded.fromCode(CreditHistory.class, row.getInteger(7)),
                Coded.fromCode(Purpose.class, row.getInteger(8)),
                Coded.fromCode(Savings.class, row.getInteger(9)),
                Coded.fromCode(OtherDebtors.class, row.getInteger(10)));

        ScoreResult result = scorecards.active().score(applicant);

        assertThat(result.score()).isEqualTo(row.getInteger(11));
        assertThat(result.probabilityOfDefault()).isCloseTo(row.getDouble(12), within(1e-9));
        assertThat(result.decision()).isEqualTo(row.getBoolean(13) ? Decision.APPROVE : Decision.REJECT);
        List<String> expectedReasons = Stream.of(row.getString(14), row.getString(15), row.getString(16))
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .toList();
        assertThat(result.reasons()).extracting(r -> r.characteristic().key())
                .containsExactlyElementsOf(expectedReasons);
        assertThat(result.lines()).extracting(ScoreResult.Line::characteristic)
                .containsExactly(Characteristic.values());
    }
}
