package ru.ifmo.se.restaurant.order.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilTest {

    @Test
    void createPageable_ShouldReturnValidPageable() {
        Pageable pageable = PaginationUtil.createPageable(0, 10);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldLimitMaxSize() {
        Pageable pageable = PaginationUtil.createPageable(0, 100);
        assertEquals(PaginationUtil.MAX_PAGE_SIZE, pageable.getPageSize());
    }

    @Test
    void createPageable_ShouldEnforceMinSize() {
        Pageable pageable = PaginationUtil.createPageable(0, 0);
        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void createPageable_WithSort_ShouldApplySort() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PaginationUtil.createPageable(1, 20, sort);
        assertEquals(1, pageable.getPageNumber());
        assertTrue(pageable.getSort().isSorted());
    }

    @Test
    void createDefaultPageable_ShouldReturnDefaultValues() {
        Pageable pageable = PaginationUtil.createDefaultPageable();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(PaginationUtil.DEFAULT_PAGE_SIZE, pageable.getPageSize());
    }

    @Test
    void createDefaultPageable_WithSort_ShouldApplySort() {
        Sort sort = Sort.by("id");
        Pageable pageable = PaginationUtil.createDefaultPageable(sort);
        assertTrue(pageable.getSort().isSorted());
    }

    @Test
    void constants_ShouldHaveExpectedValues() {
        assertEquals(50, PaginationUtil.MAX_PAGE_SIZE);
        assertEquals(20, PaginationUtil.DEFAULT_PAGE_SIZE);
    }
}
