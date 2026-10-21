package io.github.davidemcbb.creditscoring.scoring;

/**
 * The categorical inputs of the scorecard. Each value keeps the code of the original data,
 * so the classes stored in the database can refer to codes. Amounts in the data are in
 * Deutsche Mark (1973-1975).
 */
public final class Inputs {

    private Inputs() {
    }

    public enum CheckingAccount implements Coded {
        NO_CHECKING_ACCOUNT(1), NEGATIVE_BALANCE(2), BALANCE_UNDER_200(3), BALANCE_200_OR_SALARY(4);

        private final int code;

        CheckingAccount(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    public enum CreditHistory implements Coded {
        PAST_DELAYS(0), CRITICAL_ACCOUNT(1), NO_CREDITS_OR_ALL_PAID(2), EXISTING_PAID_DULY(3),
        ALL_PAID_AT_THIS_BANK(4);

        private final int code;

        CreditHistory(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    public enum Purpose implements Coded {
        OTHERS(0), NEW_CAR(1), USED_CAR(2), FURNITURE(3), RADIO_TV(4), DOMESTIC_APPLIANCES(5),
        REPAIRS(6), EDUCATION(7), VACATION(8), RETRAINING(9), BUSINESS(10);

        private final int code;

        Purpose(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    public enum Savings implements Coded {
        NONE_OR_UNKNOWN(1), UNDER_100(2), FROM_100_TO_500(3), FROM_500_TO_1000(4), OVER_1000(5);

        private final int code;

        Savings(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    public enum EmploymentDuration implements Coded {
        UNEMPLOYED(1), UNDER_1_YEAR(2), FROM_1_TO_4_YEARS(3), FROM_4_TO_7_YEARS(4), OVER_7_YEARS(5);

        private final int code;

        EmploymentDuration(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    /** Instalment as a share of disposable income. */
    public enum InstallmentRate implements Coded {
        OVER_35_PERCENT(1), FROM_25_TO_35_PERCENT(2), FROM_20_TO_25_PERCENT(3), UNDER_20_PERCENT(4);

        private final int code;

        InstallmentRate(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }

    public enum OtherDebtors implements Coded {
        NONE(1), CO_APPLICANT(2), GUARANTOR(3);

        private final int code;

        OtherDebtors(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }
    }
}
