package ru.ifmo.se.restaurant.order.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationUtilTest {

    @Test
    void constructor_ThrowsException() {
        assertThatThrownBy(() -> {
            var constructor = PaginationUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void createPageable_WithValidInputs_CreatesPageable() {
        Pageable result = PaginationUtil.createPageable(0, 20);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
    }

    @Test
    void createPageable_WithNegativePage_UsesZero() {
        Pageable result = PaginationUtil.createPageable(-5, 20);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
    }

    @Test
    void createPageable_WithSizeAboveMax_UsesMaxSize() {
        Pageable result = PaginationUtil.createPageable(0, 100);

        assertThat(result.getPageSize()).isEqualTo(50); // MAX_PAGE_SIZE
    }

    @Test
    void createPageable_WithZeroSize_UsesOne() {
        Pageable result = PaginationUtil.createPageable(0, 0);

        assertThat(result.getPageSize()).isEqualTo(1);
    }

    @Test
    void createPageable_WithSort_CreatesPageableWithSort() {
        Sort sort = Sort.by("id").descending();
        Pageable result = PaginationUtil.createPageable(0, 20, sort);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getSort()).isEqualTo(sort);
    }

    @Test
    void createDefaultPageable_CreatesDefaultPageable() {
        Pageable result = PaginationUtil.createDefaultPageable();

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20); // DEFAULT_PAGE_SIZE
    }

    @Test
    void createDefaultPageable_WithSort_CreatesDefaultPageableWithSort() {
        Sort sort = Sort.by("createdAt").ascending();
        Pageable result = PaginationUtil.createDefaultPageable(sort);

        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getSort()).isEqualTo(sort);
    }
}
