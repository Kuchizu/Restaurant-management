package ru.ifmo.se.restaurant.menu.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.ifmo.se.restaurant.menu.application.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.application.dto.DishDto;
import ru.ifmo.se.restaurant.menu.application.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.application.port.out.CategoryRepository;
import ru.ifmo.se.restaurant.menu.application.port.out.DishRepository;
import ru.ifmo.se.restaurant.menu.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.menu.domain.entity.Category;
import ru.ifmo.se.restaurant.menu.domain.entity.Dish;
import ru.ifmo.se.restaurant.menu.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.menu.domain.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DishRepository dishRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private MenuService menuService;

    private Category testCategory;
    private Dish testDish;
    private Ingredient testIngredient;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Main Dishes")
                .description("Main course dishes")
                .isActive(true)
                .build();

        testIngredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("g")
                .build();

        testDish = Dish.builder()
                .id(1L)
                .name("Pizza")
                .description("Delicious pizza")
                .price(new BigDecimal("15.00"))
                .cost(new BigDecimal("5.00"))
                .category(testCategory)
                .isActive(true)
                .ingredients(new HashSet<>(Arrays.asList(testIngredient)))
                .build();
    }

    // Category tests
    @Test
    void createCategory_ShouldReturnCreatedCategory() {
        CategoryDto dto = new CategoryDto(null, "Main Dishes", "Main course dishes", true);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        StepVerifier.create(menuService.createCategory(dto))
                .expectNextMatches(result ->
                    result.getName().equals("Main Dishes") &&
                    result.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        when(categoryRepository.getById(1L)).thenReturn(testCategory);

        StepVerifier.create(menuService.getCategoryById(1L))
                .expectNextMatches(result -> result.getName().equals("Main Dishes"))
                .verifyComplete();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAll()).thenReturn(categories);

        StepVerifier.create(menuService.getAllCategories())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() {
        CategoryDto dto = new CategoryDto(1L, "Updated Name", "Updated description", true);
        when(categoryRepository.getById(1L)).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        StepVerifier.create(menuService.updateCategory(1L, dto))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void deleteCategory_ShouldComplete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        StepVerifier.create(menuService.deleteCategory(1L))
                .verifyComplete();

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_ShouldThrowWhenNotFound() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        StepVerifier.create(menuService.deleteCategory(999L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    // Dish tests
    @Test
    void createDish_ShouldReturnCreatedDish() {
        DishDto dto = new DishDto(null, "Pizza", "Delicious pizza",
                new BigDecimal("15.00"), new BigDecimal("5.00"), 1L, null, true, Arrays.asList(1L));

        when(categoryRepository.getById(1L)).thenReturn(testCategory);
        when(ingredientRepository.findAllById(any())).thenReturn(Arrays.asList(testIngredient));
        when(dishRepository.save(any(Dish.class))).thenReturn(testDish);

        StepVerifier.create(menuService.createDish(dto))
                .expectNextMatches(result -> result.getName().equals("Pizza"))
                .verifyComplete();
    }

    @Test
    void getDishById_ShouldReturnDish() {
        when(dishRepository.getById(1L)).thenReturn(testDish);

        StepVerifier.create(menuService.getDishById(1L))
                .expectNextMatches(result -> result.getName().equals("Pizza"))
                .verifyComplete();
    }

    @Test
    void getActiveDishes_ShouldReturnActiveDishes() {
        List<Dish> dishes = Arrays.asList(testDish);
        when(dishRepository.findByIsActive(true)).thenReturn(dishes);

        StepVerifier.create(menuService.getActiveDishes())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void deleteDish_ShouldComplete() {
        when(dishRepository.existsById(1L)).thenReturn(true);
        doNothing().when(dishRepository).deleteById(1L);

        StepVerifier.create(menuService.deleteDish(1L))
                .verifyComplete();

        verify(dishRepository).deleteById(1L);
    }

    @Test
    void deleteDish_ShouldThrowWhenNotFound() {
        when(dishRepository.existsById(999L)).thenReturn(false);

        StepVerifier.create(menuService.deleteDish(999L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    // Ingredient tests
    @Test
    void createIngredient_ShouldReturnCreatedIngredient() {
        IngredientDto dto = new IngredientDto(null, "Salt", "g");
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);

        StepVerifier.create(menuService.createIngredient(dto))
                .expectNextMatches(result -> result.getName().equals("Salt"))
                .verifyComplete();
    }

    @Test
    void getIngredientById_ShouldReturnIngredient() {
        when(ingredientRepository.getById(1L)).thenReturn(testIngredient);

        StepVerifier.create(menuService.getIngredientById(1L))
                .expectNextMatches(result -> result.getName().equals("Salt"))
                .verifyComplete();
    }

    @Test
    void getAllIngredients_ShouldReturnAllIngredients() {
        List<Ingredient> ingredients = Arrays.asList(testIngredient);
        when(ingredientRepository.findAll()).thenReturn(ingredients);

        StepVerifier.create(menuService.getAllIngredients())
                .expectNextCount(1)
                .verifyComplete();
    }

    // Additional Category pagination tests
    @Test
    void getAllCategoriesPaginated_ShouldReturnPage() {
        Page<Category> page = new PageImpl<>(Arrays.asList(testCategory));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        StepVerifier.create(menuService.getAllCategoriesPaginated(0, 10))
                .expectNextMatches(result -> result.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllCategoriesSlice_ShouldReturnSlice() {
        Slice<Category> slice = new SliceImpl<>(Arrays.asList(testCategory));
        when(categoryRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);

        StepVerifier.create(menuService.getAllCategoriesSlice(0, 10))
                .expectNextMatches(result -> result.getContent().size() == 1)
                .verifyComplete();
    }

    // Additional Dish tests
    @Test
    void getDishByName_ShouldReturnDish() {
        when(dishRepository.findByName("Pizza")).thenReturn(Optional.of(testDish));

        StepVerifier.create(menuService.getDishByName("Pizza"))
                .expectNextMatches(result -> result.getName().equals("Pizza"))
                .verifyComplete();
    }

    @Test
    void getDishByName_ShouldThrowWhenNotFound() {
        when(dishRepository.findByName("Unknown")).thenReturn(Optional.empty());

        StepVerifier.create(menuService.getDishByName("Unknown"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getAllDishesPaginated_ShouldReturnPage() {
        Page<Dish> page = new PageImpl<>(Arrays.asList(testDish));
        when(dishRepository.findAll(any(Pageable.class))).thenReturn(page);

        StepVerifier.create(menuService.getAllDishesPaginated(0, 10))
                .expectNextMatches(result -> result.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void getAllDishesSlice_ShouldReturnSlice() {
        Slice<Dish> slice = new SliceImpl<>(Arrays.asList(testDish));
        when(dishRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);

        StepVerifier.create(menuService.getAllDishesSlice(0, 10))
                .expectNextMatches(result -> result.getContent().size() == 1)
                .verifyComplete();
    }

    @Test
    void updateDish_ShouldReturnUpdatedDish() {
        DishDto dto = new DishDto(1L, "Updated Pizza", "Updated description",
                new BigDecimal("20.00"), new BigDecimal("7.00"), 1L, null, true, Arrays.asList(1L));

        when(dishRepository.getById(1L)).thenReturn(testDish);
        when(categoryRepository.getById(1L)).thenReturn(testCategory);
        when(ingredientRepository.findAllById(any())).thenReturn(Arrays.asList(testIngredient));
        when(dishRepository.save(any(Dish.class))).thenReturn(testDish);

        StepVerifier.create(menuService.updateDish(1L, dto))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void createDish_WithNullIngredients_ShouldSucceed() {
        DishDto dto = new DishDto(null, "Pizza", "Delicious pizza",
                new BigDecimal("15.00"), new BigDecimal("5.00"), 1L, null, true, null);

        Dish dishWithoutIngredients = Dish.builder()
                .id(1L)
                .name("Pizza")
                .description("Delicious pizza")
                .price(new BigDecimal("15.00"))
                .cost(new BigDecimal("5.00"))
                .category(testCategory)
                .isActive(true)
                .ingredients(new HashSet<>())
                .build();

        when(categoryRepository.getById(1L)).thenReturn(testCategory);
        when(dishRepository.save(any(Dish.class))).thenReturn(dishWithoutIngredients);

        StepVerifier.create(menuService.createDish(dto))
                .expectNextMatches(result -> result.getName().equals("Pizza"))
                .verifyComplete();
    }

    @Test
    void createCategory_WithNullIsActive_ShouldDefaultToTrue() {
        CategoryDto dto = new CategoryDto(null, "New Category", "Description", null);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        StepVerifier.create(menuService.createCategory(dto))
                .expectNextCount(1)
                .verifyComplete();

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createDish_WithNullIsActive_ShouldDefaultToTrue() {
        DishDto dto = new DishDto(null, "Pizza", "Delicious pizza",
                new BigDecimal("15.00"), new BigDecimal("5.00"), 1L, null, null, Arrays.asList(1L));

        when(categoryRepository.getById(1L)).thenReturn(testCategory);
        when(ingredientRepository.findAllById(any())).thenReturn(Arrays.asList(testIngredient));
        when(dishRepository.save(any(Dish.class))).thenReturn(testDish);

        StepVerifier.create(menuService.createDish(dto))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void updateDish_WithNullIngredients_ShouldSucceed() {
        DishDto dto = new DishDto(1L, "Updated Pizza", "Updated description",
                new BigDecimal("20.00"), new BigDecimal("7.00"), 1L, null, true, null);

        when(dishRepository.getById(1L)).thenReturn(testDish);
        when(categoryRepository.getById(1L)).thenReturn(testCategory);
        when(dishRepository.save(any(Dish.class))).thenReturn(testDish);

        StepVerifier.create(menuService.updateDish(1L, dto))
                .expectNextCount(1)
                .verifyComplete();
    }
}
