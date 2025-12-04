package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.model.entity.Category;
import ru.ifmo.se.restaurant.model.entity.Dish;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.model.entity.OrderItem;
import ru.ifmo.se.restaurant.repository.CategoryRepository;
import ru.ifmo.se.restaurant.repository.DishRepository;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest extends BaseIntegrationTest {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long ingredientId;

    @BeforeEach
    void setUp() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Tomato");
        ingredient.setUnit("kg");
        Ingredient saved = ingredientRepository.save(ingredient);
        ingredientId = saved.getId();
    }

    @Test
    void testAddInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(100);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.50"));
        dto.setExpiryDate(LocalDate.now().plusDays(7));

        InventoryDto created = inventoryService.addInventory(dto);
        assertNotNull(created.getId());
        assertEquals(100, created.getQuantity());
    }

    @Test
    void testGetInventoryById() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(50);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("3.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(5));
        InventoryDto created = inventoryService.addInventory(dto);

        InventoryDto found = inventoryService.getInventoryById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals(50, found.getQuantity());
    }

    @Test
    void testUpdateInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(75);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(10));
        InventoryDto created = inventoryService.addInventory(dto);

        created.setQuantity(100);
        InventoryDto updated = inventoryService.updateInventory(created.getId(), created);
        assertEquals(100, updated.getQuantity());
    }

    @Test
    void testGetAllInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(50);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.50"));
        dto.setExpiryDate(LocalDate.now().plusDays(7));
        inventoryService.addInventory(dto);

        var inventory = inventoryService.getAllInventory(0, 10);
        assertFalse(inventory.isEmpty());
    }

    @Test
    void testGetExpiringInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(50);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.50"));
        dto.setExpiryDate(LocalDate.now().plusDays(2));
        inventoryService.addInventory(dto);

        var expiring = inventoryService.getExpiringInventory(LocalDate.now().plusDays(7));
        assertFalse(expiring.isEmpty());
    }

    @Test
    void testDeleteInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(50);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.50"));
        dto.setExpiryDate(LocalDate.now().plusDays(7));
        InventoryDto created = inventoryService.addInventory(dto);

        inventoryService.deleteInventory(created.getId());
        assertThrows(ru.ifmo.se.restaurant.exception.ResourceNotFoundException.class, () ->
            inventoryService.getInventoryById(created.getId()));
    }

    @Test
    void testAddInventoryWithInvalidIngredient() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(99999L);
        dto.setQuantity(50);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.50"));
        dto.setExpiryDate(LocalDate.now().plusDays(7));

        assertThrows(ru.ifmo.se.restaurant.exception.ResourceNotFoundException.class, () ->
            inventoryService.addInventory(dto));
    }

    @Test
    void testUpdateInventoryNotFound() {
        InventoryDto dto = new InventoryDto();
        dto.setQuantity(100);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("3.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(10));

        assertThrows(ru.ifmo.se.restaurant.exception.ResourceNotFoundException.class, () ->
            inventoryService.updateInventory(99999L, dto));
    }

    @Test
    void testDeleteInventoryNotFound() {
        assertThrows(ru.ifmo.se.restaurant.exception.ResourceNotFoundException.class, () ->
            inventoryService.deleteInventory(99999L));
    }

    @Test
    void testGetInventoryByIdNotFound() {
        assertThrows(ru.ifmo.se.restaurant.exception.ResourceNotFoundException.class, () ->
            inventoryService.getInventoryById(99999L));
    }

    @Test
    void testAddInventoryWithDefaultReceivedDate() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(75);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("3.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(14));

        InventoryDto created = inventoryService.addInventory(dto);
        assertNotNull(created.getId());
        assertEquals(75, created.getQuantity());
        assertNotNull(created.getReceivedDate());
    }

    @Test
    void testUpdateInventoryWithPriceChange() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(ingredientId);
        dto.setQuantity(100);
        dto.setReservedQuantity(0);
        dto.setPricePerUnit(new BigDecimal("2.00"));
        dto.setExpiryDate(LocalDate.now().plusDays(7));
        InventoryDto created = inventoryService.addInventory(dto);

        created.setPricePerUnit(new BigDecimal("2.50"));
        InventoryDto updated = inventoryService.updateInventory(created.getId(), created);
        assertEquals(new BigDecimal("2.50"), updated.getPricePerUnit());
    }

    @Test
    void testReserveIngredientsForOrder() {
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow();

        InventoryDto invDto = new InventoryDto();
        invDto.setIngredientId(ingredientId);
        invDto.setQuantity(100);
        invDto.setReservedQuantity(0);
        invDto.setPricePerUnit(new BigDecimal("2.00"));
        invDto.setExpiryDate(LocalDate.now().plusDays(30));
        inventoryService.addInventory(invDto);

        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        Dish dish = new Dish();
        dish.setName("Test Dish");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategory(category);
        dish.setIngredients(List.of(ingredient));
        dish = dishRepository.save(dish);

        OrderItem orderItem = new OrderItem();
        orderItem.setDish(dish);
        orderItem.setQuantity(2);
        orderItem.setPrice(dish.getPrice());

        List<OrderItem> orderItems = List.of(orderItem);

        assertDoesNotThrow(() -> inventoryService.reserveIngredientsForOrder(orderItems));
    }

    @Test
    void testReserveIngredientsWithInsufficientInventory() {
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow();

        InventoryDto invDto = new InventoryDto();
        invDto.setIngredientId(ingredientId);
        invDto.setQuantity(1);
        invDto.setReservedQuantity(0);
        invDto.setPricePerUnit(new BigDecimal("2.00"));
        invDto.setExpiryDate(LocalDate.now().plusDays(30));
        inventoryService.addInventory(invDto);

        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        Dish dish = new Dish();
        dish.setName("Test Dish");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategory(category);
        dish.setIngredients(List.of(ingredient));
        dish = dishRepository.save(dish);

        OrderItem orderItem = new OrderItem();
        orderItem.setDish(dish);
        orderItem.setQuantity(10);
        orderItem.setPrice(dish.getPrice());

        List<OrderItem> orderItems = List.of(orderItem);

        assertThrows(BusinessException.class, () ->
            inventoryService.reserveIngredientsForOrder(orderItems));
    }

    @Test
    void testConsumeReservedIngredients() {
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow();

        InventoryDto invDto = new InventoryDto();
        invDto.setIngredientId(ingredientId);
        invDto.setQuantity(100);
        invDto.setReservedQuantity(0);
        invDto.setPricePerUnit(new BigDecimal("2.00"));
        invDto.setExpiryDate(LocalDate.now().plusDays(30));
        inventoryService.addInventory(invDto);

        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        Dish dish = new Dish();
        dish.setName("Test Dish");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategory(category);
        dish.setIngredients(List.of(ingredient));
        dish = dishRepository.save(dish);

        OrderItem orderItem = new OrderItem();
        orderItem.setDish(dish);
        orderItem.setQuantity(2);
        orderItem.setPrice(dish.getPrice());

        List<OrderItem> orderItems = List.of(orderItem);

        inventoryService.reserveIngredientsForOrder(orderItems);
        assertDoesNotThrow(() -> inventoryService.consumeReservedIngredients(orderItems));
    }

    @Test
    void testReserveIngredientsWithEmptyIngredientsList() {
        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        Dish dish = new Dish();
        dish.setName("Test Dish");
        dish.setPrice(new BigDecimal("10.00"));
        dish.setCost(new BigDecimal("5.00"));
        dish.setCategory(category);
        dish.setIngredients(new ArrayList<>());
        dish = dishRepository.save(dish);

        OrderItem orderItem = new OrderItem();
        orderItem.setDish(dish);
        orderItem.setQuantity(2);
        orderItem.setPrice(dish.getPrice());

        List<OrderItem> orderItems = List.of(orderItem);

        assertDoesNotThrow(() -> inventoryService.reserveIngredientsForOrder(orderItems));
    }
}

