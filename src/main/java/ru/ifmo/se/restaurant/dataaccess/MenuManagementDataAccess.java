package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.model.entity.Category;
import ru.ifmo.se.restaurant.model.entity.Dish;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.repository.CategoryRepository;
import ru.ifmo.se.restaurant.repository.DishRepository;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import java.util.List;
import java.util.Optional;

@Component
public class MenuManagementDataAccess {
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    public MenuManagementDataAccess(CategoryRepository categoryRepository,
                                    DishRepository dishRepository,
                                    IngredientRepository ingredientRepository) {
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
    }
    
    public Category saveCategory(Category category) { return categoryRepository.save(category); }
    public Optional<Category> findCategoryById(Long id) { return categoryRepository.findById(id); }
    public Page<Category> findActiveCategories(Pageable pageable) { return categoryRepository.findByIsActiveTrue(pageable); }
    public void deleteCategory(Category category) { categoryRepository.delete(category); }
    
    public Dish saveDish(Dish dish) { return dishRepository.save(dish); }
    public Optional<Dish> findActiveDishById(Long id) { return dishRepository.findByIdAndIsActiveTrue(id); }
    public Page<Dish> findActiveDishes(Pageable pageable) { return dishRepository.findByIsActiveTrue(pageable); }
    public Page<Dish> findDishesByCategory(Long categoryId, Pageable pageable) { return dishRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable); }
    public List<Ingredient> findIngredientsByIds(List<Long> ids) { return ingredientRepository.findAllById(ids); }
}
