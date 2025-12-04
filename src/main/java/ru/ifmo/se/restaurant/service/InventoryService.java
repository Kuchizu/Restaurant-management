package ru.ifmo.se.restaurant.service;

import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Inventory;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.model.entity.OrderItem;
import ru.ifmo.se.restaurant.repository.IngredientRepository;
import ru.ifmo.se.restaurant.repository.InventoryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                           IngredientRepository ingredientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional
    public InventoryDto addInventory(InventoryDto dto) {
        Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + dto.getIngredientId()));

        Inventory inventory = new Inventory();
        inventory.setIngredient(ingredient);
        inventory.setQuantity(dto.getQuantity());
        inventory.setReservedQuantity(dto.getReservedQuantity() != null ? dto.getReservedQuantity() : 0);
        inventory.setPricePerUnit(dto.getPricePerUnit());
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setReceivedDate(dto.getReceivedDate() != null ? dto.getReceivedDate() : LocalDate.now());

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public void reserveIngredientsForOrder(List<OrderItem> orderItems) {
        try {
            for (OrderItem item : orderItems) {
                if (item.getDish().getIngredients() != null) {
                    for (Ingredient ingredient : item.getDish().getIngredients()) {
                        Integer requiredQuantity = item.getQuantity();
                        List<Inventory> availableInventories = inventoryRepository.findAvailableForReservation(
                            ingredient.getId(),
                            requiredQuantity,
                            LocalDate.now()
                        );

                        if (availableInventories.isEmpty()) {
                            throw new BusinessException("Insufficient inventory for ingredient: " + ingredient.getName());
                        }

                        int remainingQuantity = requiredQuantity;
                        for (Inventory inventory : availableInventories) {
                            int available = inventory.getQuantity() - inventory.getReservedQuantity();
                            if (remainingQuantity <= 0) break;

                            int toReserve = Math.min(remainingQuantity, available);
                            inventory.setReservedQuantity(inventory.getReservedQuantity() + toReserve);
                            remainingQuantity -= toReserve;
                            Inventory saved = inventoryRepository.save(inventory);
                            inventoryRepository.flush();
                        }

                        if (remainingQuantity > 0) {
                            throw new BusinessException("Insufficient inventory for ingredient: " + ingredient.getName());
                        }
                    }
                }
            }
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            throw new BusinessException("Inventory was updated by another process. Please try again.");
        }
    }

    @Transactional
    public void consumeReservedIngredients(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getDish().getIngredients() != null) {
                for (Ingredient ingredient : item.getDish().getIngredients()) {
                    Integer requiredQuantity = item.getQuantity();
                    List<Inventory> inventories = inventoryRepository.findByIngredientId(ingredient.getId());

                    int remainingQuantity = requiredQuantity;
                    for (Inventory inventory : inventories) {
                        if (remainingQuantity <= 0) break;
                        if (inventory.getReservedQuantity() <= 0) continue;

                        int toConsume = Math.min(remainingQuantity, inventory.getReservedQuantity());
                        inventory.setReservedQuantity(inventory.getReservedQuantity() - toConsume);
                        inventory.setQuantity(inventory.getQuantity() - toConsume);
                        remainingQuantity -= toConsume;
                        inventoryRepository.save(inventory);
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public InventoryDto getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return toDto(inventory);
    }

    @Transactional(readOnly = true)
    public Page<InventoryDto> getAllInventory(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return inventoryRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<InventoryDto> getExpiringInventory(LocalDate date) {
        return inventoryRepository.findExpiringSoon(date).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public InventoryDto updateInventory(Long id, InventoryDto dto) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        if (dto.getQuantity() != null) {
            inventory.setQuantity(dto.getQuantity());
        }
        if (dto.getReservedQuantity() != null) {
            inventory.setReservedQuantity(dto.getReservedQuantity());
        }
        if (dto.getPricePerUnit() != null) {
            inventory.setPricePerUnit(dto.getPricePerUnit());
        }
        if (dto.getExpiryDate() != null) {
            inventory.setExpiryDate(dto.getExpiryDate());
        }

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        inventoryRepository.delete(inventory);
    }

    private InventoryDto toDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setIngredientId(inventory.getIngredient().getId());
        dto.setIngredientName(inventory.getIngredient().getName());
        dto.setQuantity(inventory.getQuantity());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setPricePerUnit(inventory.getPricePerUnit());
        dto.setExpiryDate(inventory.getExpiryDate());
        dto.setReceivedDate(inventory.getReceivedDate());
        return dto;
    }
}

