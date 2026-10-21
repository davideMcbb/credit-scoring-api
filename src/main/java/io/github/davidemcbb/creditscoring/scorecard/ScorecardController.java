package io.github.davidemcbb.creditscoring.scorecard;

import io.github.davidemcbb.creditscoring.scoring.Rule;
import io.github.davidemcbb.creditscoring.scoring.Scorecard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scorecard")
@Tag(name = "Scorecard")
public class ScorecardController {

    private final ScorecardService service;

    public ScorecardController(ScorecardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "The active scorecard: every class of every characteristic and its points")
    public ScorecardView active() {
        return ScorecardView.of(service.active());
    }

    public record ScorecardView(String version, double factor, double offset, double pointsToDoubleOdds,
                                double cutoff, List<CharacteristicView> characteristics) {

        static ScorecardView of(Scorecard s) {
            return new ScorecardView(s.version(), s.factor(), s.offset(), s.factor() * Math.log(2),
                    s.cutoff(), s.rules().stream().map(CharacteristicView::of).toList());
        }
    }

    public record CharacteristicView(String name, Rule.Kind kind, int neutralPoints, double averagePoints,
                                     List<AttributeView> attributes) {

        static CharacteristicView of(Rule r) {
            List<AttributeView> attributes = r.bins().stream()
                    .map(b -> new AttributeView(b.label(), b.upperBound(),
                            b.codes().stream().sorted().map(r.characteristic()::categoryName).toList(),
                            b.points()))
                    .toList();
            return new CharacteristicView(r.characteristic().key(), r.kind(), r.neutralPoints(),
                    r.averagePoints(), attributes);
        }
    }

    public record AttributeView(String label, BigDecimal upperBound, List<String> categories, int points) {
    }
}
