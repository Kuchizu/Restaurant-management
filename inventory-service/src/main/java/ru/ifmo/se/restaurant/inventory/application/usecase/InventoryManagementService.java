package ru.ifmo.se.restaurant.inventory.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.inventory.application.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.application.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageIngredientUseCase;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageInventoryUseCase;
import ru.ifmo.se.restaurant.inventory.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryEventPublisher;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.domain.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.domain.exception.ValidationException;
import ru.ifmo.se.restaurant.inventory.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryManagementService implements ManageInventoryUseCase, ManageIngredientUseCase {

    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryEventPublisher inventoryEventPublisher;

    // Inventory Management

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getAllInventory() {
        return inventoryRepository.findAll().stream()
            .map(InventoryDto::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDto> getAllInventoryPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "ingredient.name"));
        return inventoryRepository.findAll(pageable)
            .map(InventoryDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<InventoryDto> getAllInventorySlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "ingredient.name"));
        return inventoryRepository.findAllSlice(pageable)
            .map(InventoryDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        return InventoryDto.fromDomain(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getLowStockInventory() {
        return inventoryRepository.findLowStockItems().stream()
            .map(InventoryDto::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    public InventoryDto createInventory(InventoryDto dto) {
        Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

        Inventory inventory = Inventory.builder()
            .ingredient(ingredient)
            .quantity(dto.getQuantity())
            .minQuantity(dto.getMinQuantity())
            .maxQuantity(dto.getMaxQuantity())
            .lastUpdated(LocalDateTime.now())
            .build();

        Inventory saved = inventoryRepository.save(inventory);
        return InventoryDto.fromDomain(saved);
    }

    @Override
    public InventoryDto updateInventory(Long id, InventoryDto dto) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        Inventory updated = inventory;
        if (dto.getQuantity() != null) {
            updated = updated.updateQuantity(dto.getQuantity());
        }
        if (dto.getMinQuantity() != null || dto.getMaxQuantity() != null) {
            updated = updated.updateThresholds(dto.getMinQuantity(), dto.getMaxQuantity());
        }

        Inventory saved = inventoryRepository.save(updated);

        // Check for low stock and publish event
        if (saved.isLowStock()) {
            log.warn("Low stock detected for ingredient '{}': current={}, minimum={}",
                saved.getIngredient().getName(),
                saved.getQuantity(),
                saved.getMinQuantity());
            inventoryEventPublisher.publishLowStock(saved);
        }

        return InventoryDto.fromDomain(saved);
    }

    @Override
    public InventoryDto adjustInventory(Long id, BigDecimal quantity) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        if (!inventory.canAdjust(quantity)) {
            throw new ValidationException(
                "Adjustment would result in negative inventory. Current: " +
                    inventory.getQuantity() + ", Adjustment: " + quantity,
                "quantity",
                quantity
            );
        }

        Inventory adjusted = inventory.adjustQuantity(quantity);
        Inventory saved = inventoryRepository.save(adjusted);

        // Check for low stock and publish event
        if (saved.isLowStock()) {
            log.warn("Low stock detected for ingredient '{}': current={}, minimum={}",
                saved.getIngredient().getName(),
                saved.getQuantity(),
                saved.getMinQuantity());
            inventoryEventPublisher.publishLowStock(saved);
        }

        return InventoryDto.fromDomain(saved);
    }

    @Override
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory not found");
        }
        inventoryRepository.deleteById(id);
    }

    // Ingredient Management

    @Override
    @Transactional(readOnly = true)
    public List<IngredientDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
            .map(IngredientDto::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IngredientDto> getAllIngredientsPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ingredientRepository.findAll(pageable)
            .map(IngredientDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<IngredientDto> getAllIngredientsSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ingredientRepository.findAllSlice(pageable)
            .map(IngredientDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientDto getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        return IngredientDto.fromDomain(ingredient);
    }

    @Override
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = Ingredient.builder()
            .name(dto.getName())
            .unit(dto.getUnit())
            .description(dto.getDescription())
            .build();

        Ingredient saved = ingredientRepository.save(ingredient);
        return IngredientDto.fromDomain(saved);
    }

    @Override
    public IngredientDto updateIngredient(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

        Ingredient updated = ingredient.updateInfo(dto.getName(), dto.getUnit(), dto.getDescription());
        Ingredient saved = ingredientRepository.save(updated);
        return IngredientDto.fromDomain(saved);
    }

    @Override
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingredient not found");
        }
        ingredientRepository.deleteById(id);
    }
}
