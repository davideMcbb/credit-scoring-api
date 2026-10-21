package io.github.davidemcbb.creditscoring.scoring;

import java.util.List;

/** The outcome of scoring one applicant, with the points behind it. */
public record ScoreResult(
        String scorecardVersion,
        int score,
        double probabilityOfDefault,
        Decision decision,
        List<Reason> reasons,
        List<Line> lines) {

    public enum Decision { APPROVE, REJECT }

    /** The points one characteristic contributed. */
    public record Line(Characteristic characteristic, int value, String attribute, int points) {
    }

    /** A characteristic that lowers the score, and by how much against the average applicant. */
    public record Reason(Characteristic characteristic, double pointsBelowAverage) {
    }

    public ScoreResult {
        reasons = List.copyOf(reasons);
        lines = List.copyOf(lines);
    }
}
