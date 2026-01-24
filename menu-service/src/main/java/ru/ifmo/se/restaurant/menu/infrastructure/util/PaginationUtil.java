package ru.ifmo.se.restaurant.menu.infrastructure.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private PaginationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, Sort.unsorted());
    }

    public static Pageable createPageable(int page, int size, Sort sort) {
        int validatedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int validatedPage = Math.max(page, 0);
        return PageRequest.of(validatedPage, validatedSize, sort);
    }

    public static Pageable createDefaultPageable() {
        return PageRequest.of(0, DEFAULT_PAGE_SIZE);
    }

    public static Pageable createDefaultPageable(Sort sort) {
        return PageRequest.of(0, DEFAULT_PAGE_SIZE, sort);
    }
}
