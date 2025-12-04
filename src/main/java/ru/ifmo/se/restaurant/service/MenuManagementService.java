package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.CategoryDto;
import ru.ifmo.se.restaurant.dto.DishDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.mapper.CategoryMapper;
import ru.ifmo.se.restaurant.model.entity.Category;
import ru.ifmo.se.restaurant.model.entity.Dish;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.repository.CategoryRepository;
import ru.ifmo.se.restaurant.repository.DishRepository;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuManagementService {
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryMapper categoryMapper;

    public MenuManagementService(CategoryRepository categoryRepository,
                                DishRepository dishRepository,
                                IngredientRepository ingredientRepository,
                                CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
        this.categoryMapper = categoryMapper;
    }

    // Category CRUD
    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = categoryMapper.toEntity(dto);
        category.setId(null);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> getAllCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return categoryRepository.findByIsActiveTrue(pageable)
            .map(categoryMapper::toDto);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    // Dish CRUD
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
        dish.setIsActive(true);
        
        if (dto.getIngredientIds() != null && !dto.getIngredientIds().isEmpty()) {
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientIds());
            dish.setIngredients(ingredients);
        }
        
        Dish saved = dishRepository.save(dish);
        return toDishDto(saved);
    }

    @Transactional(readOnly = true)
    public DishDto getDishById(Long id) {
        Dish dish = dishRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
        return toDishDto(dish);
    }

    @Transactional(readOnly = true)
    public Page<DishDto> getAllDishes(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return dishRepository.findByIsActiveTrue(pageable)
            .map(this::toDishDto);
    }

    @Transactional(readOnly = true)
    public Page<DishDto> getDishesByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return dishRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
            .map(this::toDishDto);
    }

    @Transactional
    public DishDto updateDish(Long id, DishDto dto) {
        Dish dish = dishRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
        
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(dish.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            dish.setCategory(category);
        }
        
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCost(dto.getCost());
        
        if (dto.getIngredientIds() != null) {
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientIds());
            dish.setIngredients(ingredients);
        }
        
        return toDishDto(dishRepository.save(dish));
    }

    @Transactional
    public void deleteDish(Long id) {
        Dish dish = dishRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
        dish.setIsActive(false);
        dishRepository.save(dish);
    }

    private DishDto toDishDto(Dish dish) {
        DishDto dto = new DishDto();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setPrice(dish.getPrice());
        dto.setCost(dish.getCost());
        dto.setCategoryId(dish.getCategory().getId());
        dto.setCategoryName(dish.getCategory().getName());
        dto.setIngredientIds(dish.getIngredients().stream().map(Ingredient::getId).collect(Collectors.toList()));
        dto.setIsActive(dish.getIsActive());
        return dto;
    }
}

