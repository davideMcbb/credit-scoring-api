package io.github.davidemcbb.creditscoring.scorecard;

import io.github.davidemcbb.creditscoring.scoring.Characteristic;
import io.github.davidemcbb.creditscoring.scoring.Rule;
import io.github.davidemcbb.creditscoring.scoring.Rule.Bin;
import io.github.davidemcbb.creditscoring.scoring.Rule.Kind;
import io.github.davidemcbb.creditscoring.scoring.Scorecard;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads the active scorecard from the database. The scorecard only changes with a new
 * migration, so it is built once and kept in the cache.
 */
@Service
public class ScorecardService {

    private final ScorecardRepository repository;

    public ScorecardService(ScorecardRepository repository) {
        this.repository = repository;
    }

    @Cacheable("activeScorecard")
    @Transactional(readOnly = true)
    public Scorecard active() {
        ScorecardEntity entity = repository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active scorecard in the database"));
        return toDomain(entity);
    }

    static Scorecard toDomain(ScorecardEntity entity) {
        List<Rule> rules = entity.getCharacteristics().stream().map(ScorecardService::toRule).toList();
        return new Scorecard(entity.getId(), entity.getVersion(), entity.getFactor().doubleValue(),
                entity.getOffsetPoints().doubleValue(), entity.getCutoff().doubleValue(), rules);
    }

    private static Rule toRule(CharacteristicEntity c) {
        Kind kind = Kind.valueOf(c.getKind());
        List<Bin> bins = c.getAttributes().stream()
                .map(a -> kind == Kind.NUMERIC
                        ? Bin.upTo(a.getLabel(), a.getUpperBound(), a.getPoints())
                        : Bin.ofCodes(a.getLabel(), a.getCodes(), a.getPoints()))
                .toList();
        return new Rule(Characteristic.fromKey(c.getName()), kind, c.getNeutralPoints(),
                c.getAveragePoints().doubleValue(), bins);
    }
}
