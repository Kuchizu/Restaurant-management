package ru.ifmo.se.restaurant.file.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class FileCategoryTest {

    @Test
    void values_ShouldContainAllCategories() {
        FileCategory[] categories = FileCategory.values();

        assertEquals(5, categories.length);
        assertArrayEquals(
                new FileCategory[]{
                        FileCategory.DISH_IMAGE,
                        FileCategory.CATEGORY_IMAGE,
                        FileCategory.RECEIPT,
                        FileCategory.REPORT,
                        FileCategory.OTHER
                },
                categories
        );
    }

    @ParameterizedTest
    @EnumSource(FileCategory.class)
    void valueOf_ShouldReturnCorrectEnum(FileCategory category) {
        FileCategory fromString = FileCategory.valueOf(category.name());
        assertEquals(category, fromString);
    }

    @Test
    void dishImage_ShouldHaveCorrectOrdinal() {
        assertEquals(0, FileCategory.DISH_IMAGE.ordinal());
    }

    @Test
    void categoryImage_ShouldHaveCorrectOrdinal() {
        assertEquals(1, FileCategory.CATEGORY_IMAGE.ordinal());
    }

    @Test
    void receipt_ShouldHaveCorrectOrdinal() {
        assertEquals(2, FileCategory.RECEIPT.ordinal());
    }

    @Test
    void report_ShouldHaveCorrectOrdinal() {
        assertEquals(3, FileCategory.REPORT.ordinal());
    }

    @Test
    void other_ShouldHaveCorrectOrdinal() {
        assertEquals(4, FileCategory.OTHER.ordinal());
    }

    @Test
    void valueOf_WithInvalidName_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> FileCategory.valueOf("INVALID"));
    }

    @Test
    void name_ShouldReturnCorrectString() {
        assertEquals("DISH_IMAGE", FileCategory.DISH_IMAGE.name());
        assertEquals("CATEGORY_IMAGE", FileCategory.CATEGORY_IMAGE.name());
        assertEquals("RECEIPT", FileCategory.RECEIPT.name());
        assertEquals("REPORT", FileCategory.REPORT.name());
        assertEquals("OTHER", FileCategory.OTHER.name());
    }
}
