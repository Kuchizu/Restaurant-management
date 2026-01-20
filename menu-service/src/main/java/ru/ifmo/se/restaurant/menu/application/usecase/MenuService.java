package ru.ifmo.se.restaurant.menu.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.ifmo.se.restaurant.menu.application.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.application.dto.DishDto;
import ru.ifmo.se.restaurant.menu.application.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.application.port.in.ManageCategoriesUseCase;
import ru.ifmo.se.restaurant.menu.application.port.in.ManageDishesUseCase;
import ru.ifmo.se.restaurant.menu.application.port.in.ManageIngredientsUseCase;
import ru.ifmo.se.restaurant.menu.application.port.out.CategoryRepository;
import ru.ifmo.se.restaurant.menu.application.port.out.DishRepository;
import ru.ifmo.se.restaurant.menu.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;
import ru.ifmo.se.restaurant.menu.domain.entity.Dish;
import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.menu.domain.exception.ValidationException;
import ru.ifmo.se.restaurant.menu.util.PaginationUtil;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService implements ManageCategoriesUseCase, ManageDishesUseCase, ManageIngredientsUseCase {
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    // Category operations
    @Override
    @Transactional
    public Mono<CategoryDto> createCategory(CategoryDto dto) {
        return Mono.fromCallable(() -> {
            Category category = Category.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                    .build();
            return toCategoryDto(categoryRepository.save(category));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<CategoryDto> getCategoryById(Long id) {
        return Mono.fromCallable(() -> {
            Category category = categoryRepository.getById(id);
            return toCategoryDto(category);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<CategoryDto> getAllCategories() {
        return Flux.defer(() -> {
            List<Category> categories = categoryRepository.findAll();
            return Flux.fromIterable(categories);
        }).map(this::toCategoryDto)
          .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Page<CategoryDto>> getAllCategoriesPaginated(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return categoryRepository.findAll(pageable)
                    .map(this::toCategoryDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Slice<CategoryDto>> getAllCategoriesSlice(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return categoryRepository.findAllSlice(pageable)
                    .map(this::toCategoryDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<CategoryDto> updateCategory(Long id, CategoryDto dto) {
        return Mono.fromCallable(() -> {
            Category existing = categoryRepository.getById(id);
            Category updated = Category.builder()
                    .id(existing.getId())
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .isActive(dto.getIsActive())
                    .build();
            return toCategoryDto(categoryRepository.save(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<Void> deleteCategory(Long id) {
        return Mono.fromRunnable(() -> {
            if (!categoryRepository.existsById(id)) {
                throw new ResourceNotFoundException("Category not found with id: " + id);
            }
            categoryRepository.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Dish operations
    @Override
    @Transactional
    public Mono<DishDto> createDish(DishDto dto) {
        return Mono.fromCallable(() -> {
            Category category = categoryRepository.getById(dto.getCategoryId());

            HashSet<Ingredient> ingredients = new HashSet<>();
            if (dto.getIngredientIds() != null && !dto.getIngredientIds().isEmpty()) {
                ingredients.addAll(ingredientRepository.findAllById(dto.getIngredientIds()));
            }

            Dish dish = Dish.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .price(dto.getPrice())
                    .cost(dto.getCost())
                    .category(category)
                    .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                    .ingredients(ingredients)
                    .build();

            return toDishDto(dishRepository.save(dish));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DishDto> getDishById(Long id) {
        return Mono.fromCallable(() -> {
            Dish dish = dishRepository.getById(id);
            return toDishDto(dish);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DishDto> getDishByName(String name) {
        return Mono.fromCallable(() -> {
            Dish dish = dishRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Dish not found with name: " + name));
            return toDishDto(dish);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Page<DishDto>> getAllDishes(Pageable pageable) {
        return Mono.fromCallable(() ->
            dishRepository.findAll(pageable).map(this::toDishDto)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Page<DishDto>> getAllDishesPaginated(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return dishRepository.findAll(pageable)
                    .map(this::toDishDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Slice<DishDto>> getAllDishesSlice(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
            return dishRepository.findAllSlice(pageable)
                    .map(this::toDishDto);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<DishDto> getActiveDishes() {
        return Flux.defer(() -> {
            List<Dish> dishes = dishRepository.findByIsActive(true);
            return Flux.fromIterable(dishes);
        }).map(this::toDishDto)
          .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<DishDto> updateDish(Long id, DishDto dto) {
        return Mono.fromCallable(() -> {
            Dish existing = dishRepository.getById(id);
            Category category = categoryRepository.getById(dto.getCategoryId());

            HashSet<Ingredient> ingredients = new HashSet<>();
            if (dto.getIngredientIds() != null) {
                ingredients.addAll(ingredientRepository.findAllById(dto.getIngredientIds()));
            }

            Dish updated = Dish.builder()
                    .id(existing.getId())
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .price(dto.getPrice())
                    .cost(dto.getCost())
                    .category(category)
                    .isActive(dto.getIsActive())
                    .ingredients(ingredients)
                    .imageUrl(existing.getImageUrl())
                    .build();

            return toDishDto(dishRepository.save(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<Void> deleteDish(Long id) {
        return Mono.fromRunnable(() -> {
            if (!dishRepository.existsById(id)) {
                throw new ResourceNotFoundException("Dish not found with id: " + id);
            }
            dishRepository.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // Ingredient operations
    @Override
    @Transactional
    public Mono<IngredientDto> createIngredient(IngredientDto dto) {
        return Mono.fromCallable(() -> {
            Ingredient ingredient = Ingredient.builder()
                    .name(dto.getName())
                    .unit(dto.getUnit())
                    .build();
            return toIngredientDto(ingredientRepository.save(ingredient));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<IngredientDto> getIngredientById(Long id) {
        return Mono.fromCallable(() -> {
            Ingredient ingredient = ingredientRepository.getById(id);
            return toIngredientDto(ingredient);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<IngredientDto> getAllIngredients() {
        return Flux.defer(() -> {
            List<Ingredient> ingredients = ingredientRepository.findAll();
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
