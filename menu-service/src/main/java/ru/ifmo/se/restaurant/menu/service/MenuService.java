package ru.ifmo.se.restaurant.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.entity.Category;
import ru.ifmo.se.restaurant.menu.entity.Dish;
import ru.ifmo.se.restaurant.menu.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.se.restaurant.menu.dataaccess.CategoryDataAccess;
import ru.ifmo.se.restaurant.menu.dataaccess.DishDataAccess;
import ru.ifmo.se.restaurant.menu.dataaccess.IngredientDataAccess;
import ru.ifmo.se.restaurant.menu.util.PaginationUtil;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    private final CategoryDataAccess categoryDataAccess;
    private final DishDataAccess dishDataAccess;
    private final IngredientDataAccess ingredientDataAccess;

    // Category operations
    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return toCategoryDto(categoryDataAccess.save(category));
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryDataAccess.getById(id);
        return toCategoryDto(category);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryDataAccess.findAll().stream()
            .map(this::toCategoryDto)
            .collect(Collectors.toList());
    }

    public Page<CategoryDto> getAllCategoriesPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return categoryDataAccess.findAll(pageable)
                .map(this::toCategoryDto);
    }

    public Slice<CategoryDto> getAllCategoriesSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return categoryDataAccess.findAllSlice(pageable)
                .map(this::toCategoryDto);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryDataAccess.getById(id);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIsActive(dto.getIsActive());
        return toCategoryDto(categoryDataAccess.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryDataAccess.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryDataAccess.deleteById(id);
    }

    // Dish operations
    @Transactional
    public DishDto createDish(DishDto dto) {
        Category category = categoryDataAccess.getById(dto.getCategoryId());

        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCost(dto.getCost());
        dish.setCategory(category);
        dish.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        if (dto.getIngredientIds() != null && !dto.getIngredientIds().isEmpty()) {
            List<Ingredient> ingredients = ingredientDataAccess.findAllById(dto.getIngredientIds());
            dish.getIngredients().addAll(ingredients);
        }

        return toDishDto(dishDataAccess.save(dish));
    }

    public DishDto getDishById(Long id) {
        Dish dish = dishDataAccess.getById(id);
        return toDishDto(dish);
    }

    public Page<DishDto> getAllDishes(Pageable pageable) {
        return dishDataAccess.findAll(pageable).map(this::toDishDto);
    }

    public Page<DishDto> getAllDishesPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return dishDataAccess.findAll(pageable)
                .map(this::toDishDto);
    }

    public Slice<DishDto> getAllDishesSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return dishDataAccess.findAllSlice(pageable)
                .map(this::toDishDto);
    }

    public List<DishDto> getActiveDishes() {
        return dishDataAccess.findByIsActive(true).stream()
            .map(this::toDishDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public DishDto updateDish(Long id, DishDto dto) {
        Dish dish = dishDataAccess.getById(id);

        Category category = categoryDataAccess.getById(dto.getCategoryId());

        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCost(dto.getCost());
        dish.setCategory(category);
        dish.setIsActive(dto.getIsActive());

        if (dto.getIngredientIds() != null) {
            dish.getIngredients().clear();
            List<Ingredient> ingredients = ingredientDataAccess.findAllById(dto.getIngredientIds());
            dish.getIngredients().addAll(ingredients);
        }

        return toDishDto(dishDataAccess.save(dish));
    }

    @Transactional
    public void deleteDish(Long id) {
        if (!dishDataAccess.existsById(id)) {
            throw new ResourceNotFoundException("Dish not found with id: " + id);
        }
        dishDataAccess.deleteById(id);
    }

    // Ingredient operations
    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        return toIngredientDto(ingredientDataAccess.save(ingredient));
    }

    public IngredientDto getIngredientById(Long id) {
        Ingredient ingredient = ingredientDataAccess.getById(id);
        return toIngredientDto(ingredient);
    }

    public List<IngredientDto> getAllIngredients() {
        return ingredientDataAccess.findAll().stream()
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
        if (dish.getCategory() == null) {
            log.error("Dish {} has null category - data integrity issue", dish.getId());
            throw new ValidationException(
                "Dish has no category assigned",
                "category",
                null
            );
        }
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
