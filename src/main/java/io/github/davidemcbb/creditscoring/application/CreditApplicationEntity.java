package io.github.davidemcbb.creditscoring.application;

import io.github.davidemcbb.creditscoring.scorecard.ScorecardEntity;
import io.github.davidemcbb.creditscoring.scoring.Applicant;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CheckingAccount;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CreditHistory;
import io.github.davidemcbb.creditscoring.scoring.Inputs.EmploymentDuration;
import io.github.davidemcbb.creditscoring.scoring.Inputs.InstallmentRate;
import io.github.davidemcbb.creditscoring.scoring.Inputs.OtherDebtors;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Purpose;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Savings;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult;
import io.github.davidemcbb.creditscoring.scoring.ScoreResult.Decision;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

/** A scored application, stored with the points behind the score so it can be audited later. */
@Entity
@Table(name = "credit_application")
public class CreditApplicationEntity {

    static final int PD_SCALE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scorecard_id", updatable = false)
    private ScorecardEntity scorecard;

    @Column(name = "duration_months", nullable = false)
    private int durationMonths;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_duration", nullable = false)
    private EmploymentDuration employmentDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "installment_rate", nullable = false)
    private InstallmentRate installmentRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "checking_account", nullable = false)
    private CheckingAccount checkingAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_history", nullable = false)
    private CreditHistory creditHistory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Savings savings;

    @Enumerated(EnumType.STRING)
    @Column(name = "other_debtors", nullable = false)
    private OtherDebtors otherDebtors;

    @Column(nullable = false)
    private int score;

    @Column(name = "probability_of_default", nullable = false)
    private BigDecimal probabilityOfDefault;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @ElementCollection
    @CollectionTable(name = "credit_application_points", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "line")
    @BatchSize(size = 50)
    private List<PointsLine> points = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "credit_application_reason", joinColumns = @JoinColumn(name = "application_id"))
    @OrderColumn(name = "reason_rank")
    @BatchSize(size = 50)
    private List<ReasonLine> reasons = new ArrayList<>();

    protected CreditApplicationEntity() {
    }

    static CreditApplicationEntity scored(Applicant a, ScoreResult result, ScorecardEntity scorecard,
                                          Instant createdAt) {
        CreditApplicationEntity e = new CreditApplicationEntity();
        e.publicId = UUID.randomUUID();
        e.createdAt = createdAt;
        e.scorecard = scorecard;
        e.durationMonths = a.durationMonths();
        e.amount = a.amount();
        e.age = a.age();
        e.employmentDuration = a.employmentDuration();
        e.installmentRate = a.installmentRate();
        e.checkingAccount = a.checkingAccount();
        e.creditHistory = a.creditHistory();
        e.purpose = a.purpose();
        e.savings = a.savings();
        e.otherDebtors = a.otherDebtors();
        e.score = result.score();
        e.probabilityOfDefault = BigDecimal.valueOf(result.probabilityOfDefault())
                .setScale(PD_SCALE, RoundingMode.HALF_UP);
        e.decision = result.decision();
        result.lines().forEach(l -> e.points.add(
                new PointsLine(l.characteristic().key(), l.value(), l.attribute(), l.points())));
        result.reasons().forEach(r -> e.reasons.add(
                new ReasonLine(r.characteristic().key(), BigDecimal.valueOf(r.pointsBelowAverage())
                        .setScale(6, RoundingMode.HALF_UP))));
        return e;
    }

    @Embeddable
    public static class PointsLine {

        @Column(name = "characteristic", nullable = false)
        private String characteristic;

        @Column(name = "input_value", nullable = false)
        private int inputValue;

        @Column(name = "attribute_label", nullable = false)
        private String attributeLabel;

        @Column(name = "points", nullable = false)
        private int points;

        protected PointsLine() {
        }

        PointsLine(String characteristic, int inputValue, String attributeLabel, int points) {
            this.characteristic = characteristic;
            this.inputValue = inputValue;
            this.attributeLabel = attributeLabel;
            this.points = points;
        }

        public String getCharacteristic() {
            return characteristic;
        }

        public int getInputValue() {
            return inputValue;
        }

        public String getAttributeLabel() {
            return attributeLabel;
        }

        public int getPoints() {
            return points;
        }
    }

    @Embeddable
    public static class ReasonLine {

        @Column(name = "characteristic", nullable = false)
        private String characteristic;

        @Column(name = "points_below_average", nullable = false)
        private BigDecimal pointsBelowAverage;

        protected ReasonLine() {
        }

        ReasonLine(String characteristic, BigDecimal pointsBelowAverage) {
            this.characteristic = characteristic;
            this.pointsBelowAverage = pointsBelowAverage;
        }

        public String getCharacteristic() {
            return characteristic;
        }

        public BigDecimal getPointsBelowAverage() {
            return pointsBelowAverage;
        }
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ScorecardEntity getScorecard() {
        return scorecard;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public int getAmount() {
        return amount;
    }

    public int getAge() {
        return age;
    }

    public EmploymentDuration getEmploymentDuration() {
        return employmentDuration;
    }

    public InstallmentRate getInstallmentRate() {
        return installmentRate;
    }

    public CheckingAccount getCheckingAccount() {
        return checkingAccount;
    }

    public CreditHistory getCreditHistory() {
        return creditHistory;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public Savings getSavings() {
        return savings;
    }

    public OtherDebtors getOtherDebtors() {
        return otherDebtors;
    }

    public int getScore() {
        return score;
    }

    public BigDecimal getProbabilityOfDefault() {
        return probabilityOfDefault;
    }

    public Decision getDecision() {
        return decision;
    }

    public List<PointsLine> getPoints() {
        return points;
    }

    public List<ReasonLine> getReasons() {
        return reasons;
    }
}
