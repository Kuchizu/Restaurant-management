package ru.ifmo.se.restaurant.inventory.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.inventory.dataaccess.IngredientDataAccess;
import ru.ifmo.se.restaurant.inventory.dataaccess.InventoryDataAccess;
import ru.ifmo.se.restaurant.inventory.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.ifmo.se.restaurant.inventory.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryDataAccess inventoryDataAccess;

    @Mock
    private IngredientDataAccess ingredientDataAccess;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void getAllInventory_ReturnsAllInventoryDtos() {
        // Given
        List<Inventory> inventories = Arrays.asList(
                createMockInventory(1L),
                createMockInventory(2L)
        );
        when(inventoryDataAccess.findAll()).thenReturn(inventories);

        // When
        List<InventoryDto> result = inventoryService.getAllInventory();

        // Then
        assertThat(result).hasSize(2);
        verify(inventoryDataAccess).findAll();
    }

    @Test
    void getInventoryById_WhenExists_ReturnsInventoryDto() {
        // Given
        Inventory inventory = createMockInventory(1L);
        when(inventoryDataAccess.getById(1L)).thenReturn(inventory);

        // When
        InventoryDto result = inventoryService.getInventoryById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIngredientId()).isEqualTo(1L);
        verify(inventoryDataAccess).getById(1L);
    }

    @Test
    void getInventoryById_WhenNotExists_ThrowsResourceNotFoundException() {
        // Given
        when(inventoryDataAccess.getById(999L))
                .thenThrow(new ResourceNotFoundException("Inventory not found"));

        // When & Then
        assertThatThrownBy(() -> inventoryService.getInventoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLowStockInventory_ReturnsLowStockItems() {
        // Given
        Ingredient ingredient = createMockIngredient(1L);
        List<Inventory> lowStockItems = Arrays.asList(
                createLowStockInventory(1L, ingredient),
                createLowStockInventory(2L, ingredient)
        );
        when(inventoryDataAccess.findLowStockItems()).thenReturn(lowStockItems);

        // When
        List<InventoryDto> result = inventoryService.getLowStockInventory();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuantity()).isLessThan(result.get(0).getMinQuantity());
        verify(inventoryDataAccess).findLowStockItems();
    }

    @Test
    void createInventory_WithValidIngredient_CreatesInventory() {
        // Given
        InventoryDto inputDto = createMockInventoryDto(null);
        inputDto.setIngredientId(1L);
        Ingredient ingredient = createMockIngredient(1L);
        Inventory savedInventory = createMockInventory(1L, ingredient);

        when(ingredientDataAccess.getById(1L)).thenReturn(ingredient);
        when(inventoryDataAccess.save(any(Inventory.class))).thenReturn(savedInventory);

        // When
        InventoryDto result = inventoryService.createInventory(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIngredientId()).isEqualTo(1L);
        verify(ingredientDataAccess).getById(1L);
        verify(inventoryDataAccess).save(any(Inventory.class));
    }

    @Test
    void createInventory_WhenIngredientNotFound_ThrowsResourceNotFoundException() {
        // Given
        InventoryDto inputDto = createMockInventoryDto(null);
        inputDto.setIngredientId(999L);

        when(ingredientDataAccess.getById(999L))
                .thenThrow(new ResourceNotFoundException("Ingredient not found"));

        // When & Then
        assertThatThrownBy(() -> inventoryService.createInventory(inputDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(inventoryDataAccess, never()).save(any());
    }

    @Test
    void updateInventory_WhenExists_UpdatesAllFields() {
        // Given
        Inventory inventory = createMockInventory(1L);
        InventoryDto updateDto = new InventoryDto();
        updateDto.setQuantity(new BigDecimal("150.00"));
        updateDto.setMinQuantity(new BigDecimal("30.00"));
        updateDto.setMaxQuantity(new BigDecimal("250.00"));

        when(inventoryDataAccess.getById(1L)).thenReturn(inventory);
        when(inventoryDataAccess.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryDto result = inventoryService.updateInventory(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryDataAccess).getById(1L);
        verify(inventoryDataAccess).save(argThat(inv ->
                inv.getQuantity().compareTo(new BigDecimal("150.00")) == 0 &&
                        inv.getMinQuantity().compareTo(new BigDecimal("30.00")) == 0 &&
                        inv.getMaxQuantity().compareTo(new BigDecimal("250.00")) == 0
        ));
    }

    @Test
    void adjustInventory_WhenExists_AdjustsQuantity() {
        // Given
        Inventory inventory = createMockInventory(1L);
        BigDecimal initialQuantity = inventory.getQuantity();
        BigDecimal adjustment = new BigDecimal("50.00");

        when(inventoryDataAccess.getById(1L)).thenReturn(inventory);
        when(inventoryDataAccess.save(any(Inventory.class))).thenReturn(inventory);

        // When
        InventoryDto result = inventoryService.adjustInventory(1L, adjustment);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryDataAccess).save(argThat(inv ->
                inv.getQuantity().compareTo(initialQuantity.add(adjustment)) == 0
        ));
    }

    @Test
    void deleteInventory_WhenExists_DeletesInventory() {
        // Given
        when(inventoryDataAccess.existsById(1L)).thenReturn(true);
        doNothing().when(inventoryDataAccess).deleteById(1L);

        // When
        inventoryService.deleteInventory(1L);

        // Then
        verify(inventoryDataAccess).existsById(1L);
        verify(inventoryDataAccess).deleteById(1L);
    }

    @Test
    void deleteInventory_WhenNotExists_ThrowsResourceNotFoundException() {
        // Given
        when(inventoryDataAccess.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> inventoryService.deleteInventory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Inventory not found");

        verify(inventoryDataAccess, never()).deleteById(any());
    }

    @Test
    void getAllIngredients_ReturnsAllIngredientDtos() {
        // Given
        List<Ingredient> ingredients = Arrays.asList(
                createMockIngredient(1L),
                createMockIngredient(2L)
        );
        when(ingredientDataAccess.findAll()).thenReturn(ingredients);

        // When
        List<IngredientDto> result = inventoryService.getAllIngredients();

        // Then
        assertThat(result).hasSize(2);
        verify(ingredientDataAccess).findAll();
    }

    @Test
    void createIngredient_SavesIngredientAndReturnsDto() {
        // Given
        IngredientDto inputDto = createMockIngredientDto(null);
        Ingredient savedIngredient = createMockIngredient(1L);

        when(ingredientDataAccess.save(any(Ingredient.class))).thenReturn(savedIngredient);

        // When
        IngredientDto result = inventoryService.createIngredient(inputDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(ingredientDataAccess).save(any(Ingredient.class));
    }

    @Test
    void updateIngredient_WhenExists_UpdatesIngredient() {
        // Given
        Ingredient ingredient = createMockIngredient(1L);
        IngredientDto updateDto = new IngredientDto();
        updateDto.setName("Updated Ingredient");
        updateDto.setUnit("liters");
        updateDto.setDescription("Updated description");

        when(ingredientDataAccess.getById(1L)).thenReturn(ingredient);
        when(ingredientDataAccess.save(any(Ingredient.class))).thenReturn(ingredient);

        // When
        IngredientDto result = inventoryService.updateIngredient(1L, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(ingredientDataAccess).save(argThat(ing ->
                ing.getName().equals("Updated Ingredient") &&
                        ing.getUnit().equals("liters") &&
                        ing.getDescription().equals("Updated description")
        ));
    }

    @Test
    void deleteIngredient_WhenExists_DeletesIngredient() {
        // Given
        when(ingredientDataAccess.existsById(1L)).thenReturn(true);
        doNothing().when(ingredientDataAccess).deleteById(1L);

        // When
        inventoryService.deleteIngredient(1L);

        // Then
        verify(ingredientDataAccess).existsById(1L);
        verify(ingredientDataAccess).deleteById(1L);
    }

    @Test
    void toDto_WhenInventoryHasNullIngredient_ThrowsValidationException() {
        // Given
        Inventory inventory = new Inventory();
        inventory.setId(1L);
        inventory.setIngredient(null); // Null ingredient

        when(inventoryDataAccess.getById(1L)).thenReturn(inventory);

        // When & Then
        assertThatThrownBy(() -> inventoryService.getInventoryById(1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Inventory item has no ingredient assigned");
    }
}
