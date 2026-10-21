package io.github.davidemcbb.creditscoring;

import org.springframework.boot.SpringApplication;

/** Runs the service against a PostgreSQL container: {@code mvn spring-boot:test-run}. */
public class TestCreditScoringApplication {

    public static void main(String[] args) {
        SpringApplication.from(CreditScoringApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
