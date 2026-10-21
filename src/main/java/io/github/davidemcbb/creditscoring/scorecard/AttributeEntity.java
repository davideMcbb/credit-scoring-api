package io.github.davidemcbb.creditscoring.scorecard;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/** One class of a characteristic and the points it is worth. */
@Entity
@Table(name = "scorecard_attribute")
public class AttributeEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "characteristic_id")
    private CharacteristicEntity characteristic;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String label;

    @Column(name = "upper_bound")
    private BigDecimal upperBound;

    @Column(nullable = false)
    private int points;

    @ElementCollection
    @CollectionTable(name = "scorecard_attribute_code", joinColumns = @JoinColumn(name = "attribute_id"))
    @Column(name = "code")
    private Set<Integer> codes = new HashSet<>();

    protected AttributeEntity() {
    }

    public Long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getUpperBound() {
        return upperBound;
    }

    public int getPoints() {
        return points;
    }

    public Set<Integer> getCodes() {
        return codes;
    }
}
