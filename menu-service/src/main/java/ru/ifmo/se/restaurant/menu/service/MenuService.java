package ru.ifmo.se.restaurant.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.repository.CategoryRepository;
import ru.ifmo.se.restaurant.menu.repository.DishRepository;
import ru.ifmo.se.restaurant.menu.repository.IngredientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    // Category operations
    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return toCategoryDto(categoryRepository.save(category));
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toCategoryDto(category);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::toCategoryDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsActive(dto.getIsActive());
        return toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // Dish operations
    @Transactional
    public DishDto createDish(DishDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCost(dto.getCost());
        dish.setCategory(category);
        dish.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        if (dto.getIngredientIds() != null && !dto.getIngredientIds().isEmpty()) {
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientIds());
            dish.getIngredients().addAll(ingredients);
        }

        return toDishDto(dishRepository.save(dish));
    }

    public DishDto getDishById(Long id) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
        return toDishDto(dish);
    }

    public Page<DishDto> getAllDishes(Pageable pageable) {
        return dishRepository.findAll(pageable).map(this::toDishDto);
    }

    public List<DishDto> getActiveDishes() {
        return dishRepository.findByIsActive(true).stream()
            .map(this::toDishDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public DishDto updateDish(Long id, DishDto dto) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCost(dto.getCost());
        dish.setCategory(category);
        dish.setIsActive(dto.getIsActive());

        if (dto.getIngredientIds() != null) {
            dish.getIngredients().clear();
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientIds());
            dish.getIngredients().addAll(ingredients);
        }

        return toDishDto(dishRepository.save(dish));
    }

    @Transactional
    public void deleteDish(Long id) {
        if (!dishRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dish not found with id: " + id);
        }
        dishRepository.deleteById(id);
    }

    // Ingredient operations
    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        return toIngredientDto(ingredientRepository.save(ingredient));
    }

    public IngredientDto getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
        return toIngredientDto(ingredient);
    }

    public List<IngredientDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
            .map(this::toIngredientDto)
            .collect(Collectors.toList());
    }

    // Mappers
    private CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getIsActive()
        );
    }

    private DishDto toDishDto(Dish dish) {
        return new DishDto(
            dish.getId(),
            dish.getName(),
            dish.getDescription(),
            dish.getPrice(),
            dish.getCost(),
            dish.getCategory().getId(),
            dish.getCategory().getName(),
            dish.getIsActive(),
            dish.getIngredients().stream().map(Ingredient::getId).collect(Collectors.toList())
        );
    }

    private IngredientDto toIngredientDto(Ingredient ingredient) {
        return new IngredientDto(
            ingredient.getId(),
            ingredient.getName(),
            ingredient.getUnit()
        );
    }
}
