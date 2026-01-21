package ru.ifmo.se.restaurant.menu.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class DishTest {

    @Test
    void builder_ShouldCreateDish() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Test Dish")
                .description("Test Description")
                .price(new BigDecimal("15.00"))
                .cost(new BigDecimal("10.00"))
                .isActive(true)
                .ingredients(new HashSet<>())
                .imageUrl("http://example.com/image.jpg")
                .build();

        assertEquals(1L, dish.getId());
        assertEquals("Test Dish", dish.getName());
        assertEquals("Test Description", dish.getDescription());
        assertEquals(new BigDecimal("15.00"), dish.getPrice());
        assertEquals(new BigDecimal("10.00"), dish.getCost());
        assertTrue(dish.getIsActive());
        assertNotNull(dish.getIngredients());
        assertEquals("http://example.com/image.jpg", dish.getImageUrl());
    }

    @Test
    void isImage_ShouldReturnTrue_WhenImageUrlIsPresent() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(BigDecimal.TEN)
                .imageUrl("http://example.com/image.jpg")
                .build();

        assertTrue(dish.isImage());
    }

    @Test
    void isImage_ShouldReturnFalse_WhenImageUrlIsNull() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(BigDecimal.TEN)
                .imageUrl(null)
                .build();

        assertFalse(dish.isImage());
    }

    @Test
    void isImage_ShouldReturnFalse_WhenImageUrlIsEmpty() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(BigDecimal.TEN)
                .imageUrl("")
                .build();

        assertFalse(dish.isImage());
    }

    @Test
    void calculateMargin_ShouldReturnDifference() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(new BigDecimal("15.00"))
                .cost(new BigDecimal("10.00"))
                .build();

        assertEquals(new BigDecimal("5.00"), dish.calculateMargin());
    }

    @Test
    void calculateMargin_ShouldReturnZero_WhenCostIsNull() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(new BigDecimal("15.00"))
                .cost(null)
                .build();

        assertEquals(BigDecimal.ZERO, dish.calculateMargin());
    }

    @Test
    void calculateMargin_ShouldReturnZero_WhenCostIsZero() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(new BigDecimal("15.00"))
                .cost(BigDecimal.ZERO)
                .build();

        assertEquals(BigDecimal.ZERO, dish.calculateMargin());
    }

    @Test
    void calculateMarginPercentage_ShouldReturnCorrectPercentage() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(new BigDecimal("15.00"))
                .cost(new BigDecimal("10.00"))
                .build();

        BigDecimal marginPercentage = dish.calculateMarginPercentage();
        assertEquals(0, new BigDecimal("50.0000").compareTo(marginPercentage));
    }

    @Test
    void calculateMarginPercentage_ShouldReturnZero_WhenCostIsNull() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(new BigDecimal("15.00"))
                .cost(null)
                .build();

        assertEquals(BigDecimal.ZERO, dish.calculateMarginPercentage());
    }

    @Test
    void calculateMarginPercentage_ShouldReturnZero_WhenCostIsZero() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(new BigDecimal("15.00"))
                .cost(BigDecimal.ZERO)
                .build();

        assertEquals(BigDecimal.ZERO, dish.calculateMarginPercentage());
    }

    @Test
    void builder_ShouldHandleNullIngredients() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish")
                .price(BigDecimal.TEN)
                .ingredients(null)
                .build();

        assertNotNull(dish.getIngredients());
        assertTrue(dish.getIngredients().isEmpty());
    }
}
