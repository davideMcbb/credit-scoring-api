package io.github.davidemcbb.creditscoring.scoring;

import java.util.Arrays;

/** The characteristics a scorecard can use, with the names stored in the database. */
public enum Characteristic {
    DURATION("duration"),
    AMOUNT("amount"),
    AGE("age"),
    EMPLOYMENT_DURATION("employment_duration"),
    INSTALLMENT_RATE("installment_rate"),
    STATUS("status"),
    CREDIT_HISTORY("credit_history"),
    PURPOSE("purpose"),
    SAVINGS("savings"),
    OTHER_DEBTORS("other_debtors");

    private final String key;

    Characteristic(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    /** The possible values of a categorical characteristic; empty for a numeric one. */
    public Coded[] categories() {
        return switch (this) {
            case DURATION, AMOUNT, AGE -> new Coded[0];
            case EMPLOYMENT_DURATION -> Inputs.EmploymentDuration.values();
            case INSTALLMENT_RATE -> Inputs.InstallmentRate.values();
            case STATUS -> Inputs.CheckingAccount.values();
            case CREDIT_HISTORY -> Inputs.CreditHistory.values();
            case PURPOSE -> Inputs.Purpose.values();
            case SAVINGS -> Inputs.Savings.values();
            case OTHER_DEBTORS -> Inputs.OtherDebtors.values();
        };
    }

    /** The name of the category with this code, as the API spells it. */
    public String categoryName(int code) {
        return Arrays.stream(categories())
                .filter(c -> c.code() == code)
                .map(c -> ((Enum<?>) c).name())
                .findFirst()
                .orElse(String.valueOf(code));
    }

    public static Characteristic fromKey(String key) {
        return Arrays.stream(values())
                .filter(c -> c.key.equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown characteristic: " + key));
    }
}
