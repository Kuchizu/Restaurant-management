package ru.ifmo.se.restaurant.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.inventory.dataaccess.IngredientDataAccess;
import ru.ifmo.se.restaurant.inventory.dataaccess.InventoryDataAccess;
import ru.ifmo.se.restaurant.inventory.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.exception.ValidationException;
import ru.ifmo.se.restaurant.inventory.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryDataAccess inventoryDataAccess;
    private final IngredientDataAccess ingredientDataAccess;

    public List<InventoryDto> getAllInventory() {
        return inventoryDataAccess.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<InventoryDto> getAllInventoryPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "ingredient.name"));
        return inventoryDataAccess.findAll(pageable)
                .map(this::toDto);
    }

    public Slice<InventoryDto> getAllInventorySlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "ingredient.name"));
        return inventoryDataAccess.findAllSlice(pageable)
                .map(this::toDto);
    }

    public InventoryDto getInventoryById(Long id) {
        return toDto(inventoryDataAccess.getById(id));
    }

    public List<InventoryDto> getLowStockInventory() {
        return inventoryDataAccess.findLowStockItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDto createInventory(InventoryDto dto) {
        Ingredient ingredient = ingredientDataAccess.getById(dto.getIngredientId());

        Inventory inventory = new Inventory();
        inventory.setIngredient(ingredient);
        inventory.setQuantity(dto.getQuantity());
        inventory.setMinQuantity(dto.getMinQuantity());
        inventory.setMaxQuantity(dto.getMaxQuantity());
        inventory.setLastUpdated(LocalDateTime.now());

        return toDto(inventoryDataAccess.save(inventory));
    }

    @Transactional
    public InventoryDto updateInventory(Long id, InventoryDto dto) {
        Inventory inventory = inventoryDataAccess.getById(id);

        if (dto.getQuantity() != null) {
            inventory.setQuantity(dto.getQuantity());
        }
        if (dto.getMinQuantity() != null) {
            inventory.setMinQuantity(dto.getMinQuantity());
        }
        if (dto.getMaxQuantity() != null) {
            inventory.setMaxQuantity(dto.getMaxQuantity());
        }
        inventory.setLastUpdated(LocalDateTime.now());

        return toDto(inventoryDataAccess.save(inventory));
    }

    @Transactional
    public InventoryDto adjustInventory(Long id, BigDecimal quantity) {
        Inventory inventory = inventoryDataAccess.getById(id);

        BigDecimal newQuantity = inventory.getQuantity().add(quantity);

        // Prevent negative inventory
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(
                "Adjustment would result in negative inventory. Current: " +
                inventory.getQuantity() + ", Adjustment: " + quantity,
                "quantity",
                quantity
            );
        }

        inventory.setQuantity(newQuantity);
        inventory.setLastUpdated(LocalDateTime.now());

        return toDto(inventoryDataAccess.save(inventory));
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryDataAccess.existsById(id)) {
            throw new ResourceNotFoundException("Inventory not found");
        }
        inventoryDataAccess.deleteById(id);
    }

    public List<IngredientDto> getAllIngredients() {
        return ingredientDataAccess.findAll().stream()
                .map(this::toIngredientDto)
                .collect(Collectors.toList());
    }

    public Page<IngredientDto> getAllIngredientsPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ingredientDataAccess.findAll(pageable)
                .map(this::toIngredientDto);
    }

    public Slice<IngredientDto> getAllIngredientsSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ingredientDataAccess.findAllSlice(pageable)
                .map(this::toIngredientDto);
    }

    public IngredientDto getIngredientById(Long id) {
        return toIngredientDto(ingredientDataAccess.getById(id));
    }

    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        ingredient.setDescription(dto.getDescription());

        return toIngredientDto(ingredientDataAccess.save(ingredient));
    }

    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientDataAccess.getById(id);

        if (dto.getName() != null) {
            ingredient.setName(dto.getName());
        }
        if (dto.getUnit() != null) {
            ingredient.setUnit(dto.getUnit());
        }
        if (dto.getDescription() != null) {
            ingredient.setDescription(dto.getDescription());
        }

        return toIngredientDto(ingredientDataAccess.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientDataAccess.existsById(id)) {
            throw new ResourceNotFoundException("Ingredient not found");
        }
        ingredientDataAccess.deleteById(id);
    }

    private InventoryDto toDto(Inventory inventory) {
        if (inventory.getIngredient() == null) {
            log.error("Inventory {} has null ingredient - data integrity issue", inventory.getId());
            throw new ValidationException(
                "Inventory item has no ingredient assigned",
                "ingredient",
                null
            );
        }
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setIngredientId(inventory.getIngredient().getId());
        dto.setIngredientName(inventory.getIngredient().getName());
        dto.setQuantity(inventory.getQuantity());
        dto.setMinQuantity(inventory.getMinQuantity());
        dto.setMaxQuantity(inventory.getMaxQuantity());
        dto.setLastUpdated(inventory.getLastUpdated());
        return dto;
    }

    private IngredientDto toIngredientDto(Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setUnit(ingredient.getUnit());
        dto.setDescription(ingredient.getDescription());
        return dto;
    }
}
