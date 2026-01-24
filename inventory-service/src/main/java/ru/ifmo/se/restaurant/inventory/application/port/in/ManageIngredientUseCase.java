package ru.ifmo.se.restaurant.inventory.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.application.dto.IngredientDto;

import java.util.List;

public interface ManageIngredientUseCase {
    List<IngredientDto> getAllIngredients();
    Page<IngredientDto> getAllIngredientsPaginated(int page, int size);
    Slice<IngredientDto> getAllIngredientsSlice(int page, int size);
    IngredientDto getIngredientById(Long id);
    IngredientDto createIngredient(IngredientDto dto);
    IngredientDto updateIngredient(Long id, IngredientDto dto);
    void deleteIngredient(Long id);
}
