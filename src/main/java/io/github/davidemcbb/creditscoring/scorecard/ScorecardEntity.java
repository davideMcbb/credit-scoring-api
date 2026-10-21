package io.github.davidemcbb.creditscoring.scorecard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scorecard")
public class ScorecardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String version;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int pdo;

    @Column(name = "base_score", nullable = false)
    private int baseScore;

    @Column(name = "base_odds", nullable = false)
    private int baseOdds;

    @Column(nullable = false)
    private BigDecimal factor;

    @Column(name = "offset_points", nullable = false)
    private BigDecimal offsetPoints;

    @Column(name = "population_bad_rate", nullable = false)
    private BigDecimal populationBadRate;

    @Column(nullable = false)
    private BigDecimal cutoff;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "scorecard")
    @OrderBy("position")
    private List<CharacteristicEntity> characteristics = new ArrayList<>();

    protected ScorecardEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public int getPdo() {
        return pdo;
    }

    public int getBaseScore() {
        return baseScore;
    }

    public int getBaseOdds() {
        return baseOdds;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public BigDecimal getOffsetPoints() {
        return offsetPoints;
    }

    public BigDecimal getPopulationBadRate() {
        return populationBadRate;
    }

    public BigDecimal getCutoff() {
        return cutoff;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<CharacteristicEntity> getCharacteristics() {
        return characteristics;
    }
}
