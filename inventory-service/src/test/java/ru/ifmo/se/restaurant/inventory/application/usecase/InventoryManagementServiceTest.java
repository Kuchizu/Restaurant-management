package ru.ifmo.se.restaurant.inventory.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.ifmo.se.restaurant.inventory.application.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.application.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryEventPublisher;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.domain.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryManagementServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private InventoryEventPublisher inventoryEventPublisher;

    @InjectMocks
    private InventoryManagementService service;

    private Ingredient testIngredient;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testIngredient = Ingredient.builder()
                .id(1L)
                .name("Salt")
                .unit("kg")
                .description("Table salt")
                .build();

        testInventory = Inventory.builder()
                .id(1L)
                .ingredient(testIngredient)
                .quantity(new BigDecimal("100.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllInventory_ShouldReturnList() {
        when(inventoryRepository.findAll()).thenReturn(Arrays.asList(testInventory));

        List<InventoryDto> result = service.getAllInventory();

        assertEquals(1, result.size());
        assertEquals("Salt", result.get(0).getIngredientName());
    }

    @Test
    void getInventoryById_ShouldReturnInventory() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        InventoryDto result = service.getInventoryById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Salt", result.getIngredientName());
    }

    @Test
    void getInventoryById_ShouldThrow_WhenNotFound() {
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getInventoryById(999L));
    }

    @Test
    void getLowStockInventory_ShouldReturnLowStockItems() {
        Inventory lowStock = Inventory.builder()
                .id(2L)
                .ingredient(testIngredient)
                .quantity(new BigDecimal("5.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();
        when(inventoryRepository.findLowStockItems()).thenReturn(Arrays.asList(lowStock));

        List<InventoryDto> result = service.getLowStockInventory();

        assertEquals(1, result.size());
    }

    @Test
    void createInventory_ShouldCreateInventory() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(1L);
        dto.setQuantity(new BigDecimal("50.00"));
        dto.setMinQuantity(new BigDecimal("10.00"));
        dto.setMaxQuantity(new BigDecimal("200.00"));

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        InventoryDto result = service.createInventory(dto);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventory_ShouldThrow_WhenIngredientNotFound() {
        InventoryDto dto = new InventoryDto();
        dto.setIngredientId(999L);

        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createInventory(dto));
    }

    @Test
    void updateInventory_ShouldUpdateQuantity() {
        InventoryDto dto = new InventoryDto();
        dto.setQuantity(new BigDecimal("150.00"));

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryDto result = service.updateInventory(1L, dto);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void updateInventory_ShouldPublishLowStockEvent() {
        Inventory lowStockInventory = Inventory.builder()
                .id(1L)
                .ingredient(testIngredient)
                .quantity(new BigDecimal("5.00"))
                .minQuantity(new BigDecimal("10.00"))
                .maxQuantity(new BigDecimal("500.00"))
                .lastUpdated(LocalDateTime.now())
                .build();

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(new BigDecimal("5.00"));

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(lowStockInventory);
        doNothing().when(inventoryEventPublisher).publishLowStock(any());

        service.updateInventory(1L, dto);

        verify(inventoryEventPublisher).publishLowStock(any(Inventory.class));
    }

    @Test
    void adjustInventory_ShouldAdjustQuantity() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryDto result = service.adjustInventory(1L, new BigDecimal("-10.00"));

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void adjustInventory_ShouldThrow_WhenResultNegative() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        assertThrows(ValidationException.class,
                () -> service.adjustInventory(1L, new BigDecimal("-200.00")));
    }

    @Test
    void deleteInventory_ShouldDelete() {
        when(inventoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(inventoryRepository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteInventory(1L));
        verify(inventoryRepository).deleteById(1L);
    }

    @Test
    void deleteInventory_ShouldThrow_WhenNotFound() {
        when(inventoryRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteInventory(999L));
    }

    @Test
    void getAllIngredients_ShouldReturnList() {
        when(ingredientRepository.findAll()).thenReturn(Arrays.asList(testIngredient));

        List<IngredientDto> result = service.getAllIngredients();

        assertEquals(1, result.size());
        assertEquals("Salt", result.get(0).getName());
    }

    @Test
    void getIngredientById_ShouldReturnIngredient() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));

        IngredientDto result = service.getIngredientById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Salt", result.getName());
    }

    @Test
    void getIngredientById_ShouldThrow_WhenNotFound() {
        when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getIngredientById(999L));
    }

    @Test
    void createIngredient_ShouldCreateIngredient() {
        IngredientDto dto = new IngredientDto();
        dto.setName("Pepper");
        dto.setUnit("g");
        dto.setDescription("Black pepper");

        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);

        IngredientDto result = service.createIngredient(dto);

        assertNotNull(result);
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void updateIngredient_ShouldUpdateIngredient() {
        IngredientDto dto = new IngredientDto();
        dto.setName("Sea Salt");
        dto.setUnit("g");
        dto.setDescription("Premium sea salt");

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(testIngredient));
        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(inv -> inv.getArgument(0));

        IngredientDto result = service.updateIngredient(1L, dto);

        assertNotNull(result);
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void deleteIngredient_ShouldDelete() {
        when(ingredientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ingredientRepository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteIngredient(1L));
        verify(ingredientRepository).deleteById(1L);
    }

    @Test
    void deleteIngredient_ShouldThrow_WhenNotFound() {
        when(ingredientRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteIngredient(999L));
    }

    @Test
    void getAllInventoryPaginated_ShouldReturnPage() {
        Page<Inventory> page = new PageImpl<>(Arrays.asList(testInventory));
        when(inventoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<InventoryDto> result = service.getAllInventoryPaginated(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllInventorySlice_ShouldReturnSlice() {
        Slice<Inventory> slice = new SliceImpl<>(Arrays.asList(testInventory));
        when(inventoryRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);

        Slice<InventoryDto> result = service.getAllInventorySlice(0, 10);

        assertTrue(result.hasContent());
    }

    @Test
    void getAllIngredientsPaginated_ShouldReturnPage() {
        Page<Ingredient> page = new PageImpl<>(Arrays.asList(testIngredient));
        when(ingredientRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<IngredientDto> result = service.getAllIngredientsPaginated(0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllIngredientsSlice_ShouldReturnSlice() {
        Slice<Ingredient> slice = new SliceImpl<>(Arrays.asList(testIngredient));
        when(ingredientRepository.findAllSlice(any(Pageable.class))).thenReturn(slice);

        Slice<IngredientDto> result = service.getAllIngredientsSlice(0, 10);

        assertTrue(result.hasContent());
    }
}
