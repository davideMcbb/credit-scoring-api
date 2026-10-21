package io.github.davidemcbb.creditscoring.scorecard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scorecard_characteristic")
public class CharacteristicEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scorecard_id")
    private ScorecardEntity scorecard;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String kind;

    @Column(name = "neutral_points", nullable = false)
    private int neutralPoints;

    @Column(name = "average_points", nullable = false)
    private BigDecimal averagePoints;

    @OneToMany(mappedBy = "characteristic")
    @OrderBy("position")
    private List<AttributeEntity> attributes = new ArrayList<>();

    protected CharacteristicEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public String getKind() {
        return kind;
    }

    public int getNeutralPoints() {
        return neutralPoints;
    }

    public BigDecimal getAveragePoints() {
        return averagePoints;
    }

    public List<AttributeEntity> getAttributes() {
        return attributes;
    }
}
