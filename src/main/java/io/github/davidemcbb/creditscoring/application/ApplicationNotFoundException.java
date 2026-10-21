package io.github.davidemcbb.creditscoring.application;

import java.util.UUID;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(UUID id) {
        super("No application with id " + id);
    }
}
