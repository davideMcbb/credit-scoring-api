package io.github.davidemcbb.creditscoring.scoring;

import io.github.davidemcbb.creditscoring.scoring.Rule.Bin;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Line;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Reason;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A points scorecard: the score is the sum of the points of each characteristic.
 *
 * <p>Points are scaled so that {@code offset} points mean even odds in the bank's population
 * and every {@code factor * ln 2} points (the PDO) double the odds of a good loan. The
 * probability of default is therefore {@code 1 / (1 + exp((score - offset) / factor))}.
 * Applications at or above the cutoff are approved.
 */
public final class Scorecard {

    static final int MAX_REASONS = 3;
    static final String NOT_SEEN = "not seen in development (neutral points)";

    private final long id;
    private final String version;
    private final double factor;
    private final double offset;
    private final double cutoff;
    private final List<Rule> rules;

    public Scorecard(long id, String version, double factor, double offset, double cutoff, List<Rule> rules) {
        if (factor <= 0) {
            throw new IllegalArgumentException("factor must be positive");
        }
        Set<Characteristic> used = EnumSet.noneOf(Characteristic.class);
        for (Rule rule : rules) {
            if (!used.add(rule.characteristic())) {
                throw new IllegalArgumentException(rule.characteristic() + " appears twice");
            }
        }
        this.id = id;
        this.version = version;
        this.factor = factor;
        this.offset = offset;
        this.cutoff = cutoff;
        this.rules = List.copyOf(rules);
    }

    public ScoreResult score(Applicant applicant) {
        List<Line> lines = new ArrayList<>(rules.size());
        List<Reason> below = new ArrayList<>();
        int total = 0;
        for (Rule rule : rules) {
            int value = applicant.value(rule.characteristic());
            Optional<Bin> bin = rule.find(value);
            int points = bin.map(Bin::points).orElse(rule.neutralPoints());
            lines.add(new Line(rule.characteristic(), value, bin.map(Bin::label).orElse(NOT_SEEN), points));
            double gap = rule.averagePoints() - points;
            if (gap > 0) {
                below.add(new Reason(rule.characteristic(), gap));
            }
            total += points;
        }
        // List.sort is stable: equal gaps keep the order of the characteristics
        below.sort(Comparator.comparingDouble(Reason::pointsBelowAverage).reversed());
        List<Reason> reasons = below.subList(0, Math.min(MAX_REASONS, below.size()));
        Decision decision = total >= cutoff ? Decision.APPROVE : Decision.REJECT;
        return new ScoreResult(version, total, probabilityOfDefault(total), decision, reasons, lines);
    }

    public double probabilityOfDefault(int score) {
        return 1.0 / (1.0 + Math.exp((score - offset) / factor));
    }

    public long id() {
        return id;
    }

    public String version() {
        return version;
    }

    public double factor() {
        return factor;
    }

    public double offset() {
        return offset;
    }

    public double cutoff() {
        return cutoff;
    }

    public List<Rule> rules() {
        return rules;
    }
}
