package ru.ifmo.se.restaurant.menu.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilTest {

    @Test
    void createPageable() {
        Pageable pageable = PaginationUtil.createPageable(0, 20);
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void createPageableWithSort() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PaginationUtil.createPageable(0, 20, sort);
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void createDefaultPageable() {
        Pageable pageable = PaginationUtil.createDefaultPageable();
        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void createDefaultPageableWithSort() {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PaginationUtil.createDefaultPageable(sort);
        assertNotNull(pageable);
    }
}
