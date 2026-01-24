package ru.ifmo.se.restaurant.kitchen.domain.exception;

public class DishNotFoundException extends RuntimeException {
    public DishNotFoundException(String dishName) {
        super("Dish not found: " + dishName);
    }
}
