package ru.ifmo.se.restaurant.billing.util;

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
        // When
        Pageable result = PaginationUtil.createPageable(0, 20);

        // Then
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    void createPageable_WithNegativePage_UsesZero() {
        // When
        Pageable result = PaginationUtil.createPageable(-1, 20);

        // Then
        assertThat(result.getPageNumber()).isEqualTo(0);
    }

    @Test
    void createPageable_WithSizeAboveMax_UsesMaxSize() {
        // When
        Pageable result = PaginationUtil.createPageable(0, 100);

        // Then
        assertThat(result.getPageSize()).isEqualTo(50); // MAX_PAGE_SIZE
    }

    @Test
    void createPageable_WithZeroSize_UsesOne() {
        // When
        Pageable result = PaginationUtil.createPageable(0, 0);

        // Then
        assertThat(result.getPageSize()).isEqualTo(1);
    }

    @Test
    void createPageable_WithSort_CreatesPageableWithSort() {
        // Given
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        // When
        Pageable result = PaginationUtil.createPageable(0, 20, sort);

        // Then
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getSort()).isEqualTo(sort);
    }

    @Test
    void createDefaultPageable_CreatesDefaultPageable() {
        // When
        Pageable result = PaginationUtil.createDefaultPageable();

        // Then
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20); // DEFAULT_PAGE_SIZE
        assertThat(result.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    void createDefaultPageable_WithSort_CreatesDefaultPageableWithSort() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // When
        Pageable result = PaginationUtil.createDefaultPageable(sort);

        // Then
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20); // DEFAULT_PAGE_SIZE
        assertThat(result.getSort()).isEqualTo(sort);
    }
}
