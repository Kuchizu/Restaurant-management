package ru.ifmo.se.restaurant.menu.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class Dish {
    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final BigDecimal cost;
    private final Category category;
    private final Boolean isActive;
    private final Set<Ingredient> ingredients;
    private final String imageUrl;

    public static class DishBuilder {
        private Set<Ingredient> ingredients = new HashSet<>();

        public DishBuilder ingredients(Set<Ingredient> ingredients) {
            this.ingredients = ingredients != null ? ingredients : new HashSet<>();
            return this;
        }
    }

    public boolean isImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    public BigDecimal calculateMargin() {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.subtract(cost);
    }

    public BigDecimal calculateMarginPercentage() {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return calculateMargin().divide(cost, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
