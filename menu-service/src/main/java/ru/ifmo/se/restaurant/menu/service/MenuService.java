package ru.ifmo.se.restaurant.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
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
    public Mono<CategoryDto> createCategory(CategoryDto dto) {
        return Mono.fromCallable(() -> {
            Category category = new Category();
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
            return toCategoryDto(categoryDataAccess.save(category));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CategoryDto> getCategoryById(Long id) {
        return Mono.fromCallable(() -> {
            Category category = categoryDataAccess.getById(id);
            return toCategoryDto(category);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<CategoryDto> getAllCategories() {
        return Flux.defer(() -> {
            List<Category> categories = categoryDataAccess.findAll();
            return Flux.fromIterable(categories);
        }).map(this::toCategoryDto)
          .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<CategoryDto>> getAllCategoriesPaginated(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return categoryDataAccess.findAll(pageable)
                    .map(this::toCategoryDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Slice<CategoryDto>> getAllCategoriesSlice(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return categoryDataAccess.findAllSlice(pageable)
                    .map(this::toCategoryDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<CategoryDto> updateCategory(Long id, CategoryDto dto) {
        return Mono.fromCallable(() -> {
            Category category = categoryDataAccess.getById(id);
            category.setName(dto.getName());
            category.setDescription(dto.getDescription());
            category.setIsActive(dto.getIsActive());
            return toCategoryDto(categoryDataAccess.save(category));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> deleteCategory(Long id) {
        return Mono.fromRunnable(() -> {
            if (!categoryDataAccess.existsById(id)) {
                throw new ResourceNotFoundException("Category not found with id: " + id);
            }
            categoryDataAccess.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Dish operations
    @Transactional
    public Mono<DishDto> createDish(DishDto dto) {
        return Mono.fromCallable(() -> {
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<DishDto> getDishById(Long id) {
        return Mono.fromCallable(() -> {
            Dish dish = dishDataAccess.getById(id);
            return toDishDto(dish);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<DishDto> getDishByName(String name) {
        return Mono.fromCallable(() -> {
            Dish dish = dishDataAccess.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Dish not found with name: " + name));
            return toDishDto(dish);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<DishDto>> getAllDishes(Pageable pageable) {
        return Mono.fromCallable(() ->
            dishDataAccess.findAll(pageable).map(this::toDishDto)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<DishDto>> getAllDishesPaginated(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return dishDataAccess.findAll(pageable)
                    .map(this::toDishDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Slice<DishDto>> getAllDishesSlice(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return dishDataAccess.findAllSlice(pageable)
                    .map(this::toDishDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<DishDto> getActiveDishes() {
        return Flux.defer(() -> {
            List<Dish> dishes = dishDataAccess.findByIsActive(true);
            return Flux.fromIterable(dishes);
        }).map(this::toDishDto)
          .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<DishDto> updateDish(Long id, DishDto dto) {
        return Mono.fromCallable(() -> {
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
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<Void> deleteDish(Long id) {
        return Mono.fromRunnable(() -> {
            if (!dishDataAccess.existsById(id)) {
                throw new ResourceNotFoundException("Dish not found with id: " + id);
            }
            dishDataAccess.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Ingredient operations
    @Transactional
    public Mono<IngredientDto> createIngredient(IngredientDto dto) {
        return Mono.fromCallable(() -> {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(dto.getName());
            ingredient.setUnit(dto.getUnit());
            return toIngredientDto(ingredientDataAccess.save(ingredient));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<IngredientDto> getIngredientById(Long id) {
        return Mono.fromCallable(() -> {
            Ingredient ingredient = ingredientDataAccess.getById(id);
            return toIngredientDto(ingredient);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<IngredientDto> getAllIngredients() {
        return Flux.defer(() -> {
            List<Ingredient> ingredients = ingredientDataAccess.findAll();
            return Flux.fromIterable(ingredients);
        }).map(this::toIngredientDto)
          .subscribeOn(Schedulers.boundedElastic());
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
