package io.github.davidemcbb.creditscoring.scoring;

import io.github.davidemcbb.creditscoring.scoring.Inputs.CheckingAccount;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CreditHistory;
import io.github.davidemcbb.creditscoring.scoring.Inputs.EmploymentDuration;
import io.github.davidemcbb.creditscoring.scoring.Inputs.InstallmentRate;
import io.github.davidemcbb.creditscoring.scoring.Inputs.OtherDebtors;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Purpose;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Savings;
import java.util.Objects;

/** What the scorecard needs to know about a loan application. */
public record Applicant(
        int durationMonths,
        int amount,
        int age,
        EmploymentDuration employmentDuration,
        InstallmentRate installmentRate,
        CheckingAccount checkingAccount,
        CreditHistory creditHistory,
        Purpose purpose,
        Savings savings,
        OtherDebtors otherDebtors) {

    public Applicant {
        Objects.requireNonNull(employmentDuration, "employmentDuration");
        Objects.requireNonNull(installmentRate, "installmentRate");
        Objects.requireNonNull(checkingAccount, "checkingAccount");
        Objects.requireNonNull(creditHistory, "creditHistory");
        Objects.requireNonNull(purpose, "purpose");
        Objects.requireNonNull(savings, "savings");
        Objects.requireNonNull(otherDebtors, "otherDebtors");
    }

    /** The value the scorecard looks up: the number itself, or the code of a category. */
    public int value(Characteristic characteristic) {
        return switch (characteristic) {
            case DURATION -> durationMonths;
            case AMOUNT -> amount;
            case AGE -> age;
            case EMPLOYMENT_DURATION -> employmentDuration.code();
            case INSTALLMENT_RATE -> installmentRate.code();
            case STATUS -> checkingAccount.code();
            case CREDIT_HISTORY -> creditHistory.code();
            case PURPOSE -> purpose.code();
            case SAVINGS -> savings.code();
            case OTHER_DEBTORS -> otherDebtors.code();
        };
    }
}
