package io.github.davidemcbb.creditscoring.web;

import java.util.List;
import org.springframework.data.domain.Page;

/** A page of results with a stable JSON shape (Spring's Page is not meant to be serialised). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
