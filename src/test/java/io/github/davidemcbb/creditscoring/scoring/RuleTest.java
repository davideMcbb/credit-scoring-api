package io.github.davidemcbb.creditscoring.scoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.davidemcbb.creditscoring.scoring.Inputs.Purpose;
import io.github.davidemcbb.creditscoring.scoring.Rule.Bin;
import io.github.davidemcbb.creditscoring.scoring.Rule.Kind;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** A scorecard read from the database is checked before it is used. */
class RuleTest {

    @Test
    void numericBoundsMustIncrease() {
        assertThatThrownBy(() -> new Rule(Characteristic.AGE, Kind.NUMERIC, 50, 50, List.of(
                Bin.upTo("a", BigDecimal.valueOf(30), 40),
                Bin.upTo("b", BigDecimal.valueOf(25), 50),
                Bin.upTo("c", null, 60))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("increase");
    }

    @Test
    void onlyTheLastNumericClassIsOpen() {
        assertThatThrownBy(() -> new Rule(Characteristic.AGE, Kind.NUMERIC, 50, 50, List.of(
                Bin.upTo("a", null, 40),
                Bin.upTo("b", BigDecimal.valueOf(25), 50))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Rule(Characteristic.AGE, Kind.NUMERIC, 50, 50, List.of(
                Bin.upTo("a", BigDecimal.valueOf(30), 40))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCodeCannotBeInTwoClasses() {
        assertThatThrownBy(() -> new Rule(Characteristic.PURPOSE, Kind.CATEGORICAL, 50, 50, List.of(
                Bin.ofCodes("a", Set.of(0, 1), 40),
                Bin.ofCodes("b", Set.of(1, 2), 50))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("code 1");
    }

    @Test
    void aRuleNeedsClasses() {
        assertThatThrownBy(() -> new Rule(Characteristic.PURPOSE, Kind.CATEGORICAL, 50, 50, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCharacteristicAppearsOnce() {
        Rule age = new Rule(Characteristic.AGE, Kind.NUMERIC, 50, 50, List.of(Bin.upTo("all", null, 50)));
        assertThatThrownBy(() -> new Scorecard(1, "x", 28.85, 487.12, 500, List.of(age, age)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("twice");
    }

    @Test
    void codesMatchTheOriginalData() {
        for (Characteristic c : Characteristic.values()) {
            for (Coded value : c.categories()) {
                assertThat(c.categoryName(value.code())).isEqualTo(((Enum<?>) value).name());
            }
        }
        assertThat(Coded.fromCode(Purpose.class, 10)).isEqualTo(Purpose.BUSINESS);
        assertThatThrownBy(() -> Coded.fromCode(Purpose.class, 11)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void characteristicsAreFoundByTheirDatabaseName() {
        assertThat(Characteristic.fromKey("other_debtors")).isEqualTo(Characteristic.OTHER_DEBTORS);
        assertThatThrownBy(() -> Characteristic.fromKey("income")).isInstanceOf(IllegalArgumentException.class);
    }
}
