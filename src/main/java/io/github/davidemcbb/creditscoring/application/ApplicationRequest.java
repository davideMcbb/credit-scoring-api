package io.github.davidemcbb.creditscoring.application;

import io.github.davidemcbb.creditscoring.scoring.Applicant;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CheckingAccount;
import io.github.davidemcbb.creditscoring.scoring.Inputs.CreditHistory;
import io.github.davidemcbb.creditscoring.scoring.Inputs.EmploymentDuration;
import io.github.davidemcbb.creditscoring.scoring.Inputs.InstallmentRate;
import io.github.davidemcbb.creditscoring.scoring.Inputs.OtherDebtors;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Purpose;
import io.github.davidemcbb.creditscoring.scoring.Inputs.Savings;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** A loan application as the API receives it. The amount is in Deutsche Mark, as in the data. */
public record ApplicationRequest(
        @NotNull @Min(1) @Max(120) Integer durationMonths,
        @NotNull @Min(1) @Max(1_000_000) Integer amount,
        @NotNull @Min(18) @Max(100) Integer age,
        @NotNull EmploymentDuration employmentDuration,
        @NotNull InstallmentRate installmentRate,
        @NotNull CheckingAccount checkingAccount,
        @NotNull CreditHistory creditHistory,
        @NotNull Purpose purpose,
        @NotNull Savings savings,
        @NotNull OtherDebtors otherDebtors) {

    public Applicant toApplicant() {
        return new Applicant(durationMonths, amount, age, employmentDuration, installmentRate,
                checkingAccount, creditHistory, purpose, savings, otherDebtors);
    }
}
