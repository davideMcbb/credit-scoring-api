package io.github.davidemcbb.creditscoring.scoring;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * How one characteristic turns into points.
 *
 * <p>Numeric classes are ordered by their inclusive upper bound, and the last one has no
 * bound. Categorical classes list the codes they contain. A code that no class contains
 * (a category never seen when the scorecard was estimated) gets the neutral points.
 */
public record Rule(Characteristic characteristic, Kind kind, int neutralPoints,
                   double averagePoints, List<Bin> bins) {

    public enum Kind { NUMERIC, CATEGORICAL }

    public record Bin(String label, BigDecimal upperBound, Set<Integer> codes, int points) {

        public Bin {
            codes = Set.copyOf(codes);
        }

        public static Bin upTo(String label, BigDecimal upperBound, int points) {
            return new Bin(label, upperBound, Set.of(), points);
        }

        public static Bin ofCodes(String label, Set<Integer> codes, int points) {
            return new Bin(label, null, codes, points);
        }
    }

    public Rule {
        bins = List.copyOf(bins);
        if (bins.isEmpty()) {
            throw new IllegalArgumentException(characteristic + ": no classes");
        }
        if (kind == Kind.NUMERIC) {
            checkBounds(characteristic, bins);
        } else {
            checkCodes(characteristic, bins);
        }
    }

    public Optional<Bin> find(int value) {
        if (kind == Kind.NUMERIC) {
            BigDecimal v = BigDecimal.valueOf(value);
            return bins.stream()
                    .filter(b -> b.upperBound() == null || v.compareTo(b.upperBound()) <= 0)
                    .findFirst();
        }
        return bins.stream().filter(b -> b.codes().contains(value)).findFirst();
    }

    public int maxPoints() {
        return bins.stream().mapToInt(Bin::points).max().orElseThrow();
    }

    private static void checkBounds(Characteristic c, List<Bin> bins) {
        for (int i = 0; i < bins.size(); i++) {
            BigDecimal upper = bins.get(i).upperBound();
            boolean last = i == bins.size() - 1;
            if (last != (upper == null)) {
                throw new IllegalArgumentException(c + ": only the last class can be open-ended");
            }
            if (i > 0 && !last && upper.compareTo(bins.get(i - 1).upperBound()) <= 0) {
                throw new IllegalArgumentException(c + ": upper bounds must increase");
            }
        }
    }

    private static void checkCodes(Characteristic c, List<Bin> bins) {
        Set<Integer> seen = new HashSet<>();
        for (Bin bin : bins) {
            if (bin.codes().isEmpty()) {
                throw new IllegalArgumentException(c + ": class '" + bin.label() + "' has no codes");
            }
            for (int code : bin.codes()) {
                if (!seen.add(code)) {
                    throw new IllegalArgumentException(c + ": code " + code + " is in two classes");
                }
            }
        }
    }
}
