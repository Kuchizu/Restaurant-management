package ru.ifmo.se.restaurant.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.inventory.dto.IngredientDto;
import ru.ifmo.se.restaurant.inventory.dto.InventoryDto;
import ru.ifmo.se.restaurant.inventory.entity.Ingredient;
import ru.ifmo.se.restaurant.inventory.entity.Inventory;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.repository.IngredientRepository;
import ru.ifmo.se.restaurant.inventory.repository.InventoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public List<InventoryDto> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public InventoryDto getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
    }

    public List<InventoryDto> getLowStockInventory() {
        return inventoryRepository.findByQuantityLessThanMinQuantity().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDto createInventory(InventoryDto dto) {
        Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

        Inventory inventory = new Inventory();
        inventory.setIngredient(ingredient);
        inventory.setQuantity(dto.getQuantity());
        inventory.setMinQuantity(dto.getMinQuantity());
        inventory.setMaxQuantity(dto.getMaxQuantity());
        inventory.setLastUpdated(LocalDateTime.now());

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryDto updateInventory(Long id, InventoryDto dto) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

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

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryDto adjustInventory(Long id, BigDecimal quantity) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        inventory.setQuantity(inventory.getQuantity().add(quantity));
        inventory.setLastUpdated(LocalDateTime.now());

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory not found");
        }
        inventoryRepository.deleteById(id);
    }

    public List<IngredientDto> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::toIngredientDto)
                .collect(Collectors.toList());
    }

    public IngredientDto getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .map(this::toIngredientDto)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
    }

    @Transactional
    public IngredientDto createIngredient(IngredientDto dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        ingredient.setDescription(dto.getDescription());

        return toIngredientDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientDto updateIngredient(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

        if (dto.getName() != null) {
            ingredient.setName(dto.getName());
        }
        if (dto.getUnit() != null) {
            ingredient.setUnit(dto.getUnit());
        }
        if (dto.getDescription() != null) {
            ingredient.setDescription(dto.getDescription());
        }

        return toIngredientDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ingredient not found");
        }
        ingredientRepository.deleteById(id);
    }

    private InventoryDto toDto(Inventory inventory) {
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
