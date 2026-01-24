package ru.ifmo.se.restaurant.menu.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.menu.application.dto.IngredientDto;

public interface ManageIngredientsUseCase {
    Mono<IngredientDto> createIngredient(IngredientDto dto);
    Mono<IngredientDto> getIngredientById(Long id);
    Flux<IngredientDto> getAllIngredients();
}
