package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.exception.BusinessException;
import ru.ifmo.se.restaurant.model.entity.Inventory;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.model.entity.OrderItem;
import ru.ifmo.se.restaurant.dataaccess.InventoryDataAccess;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryDataAccess inventoryDataAccess;

    public InventoryService(InventoryDataAccess inventoryDataAccess) {
        this.inventoryDataAccess = inventoryDataAccess;
    }

    @Transactional
    public InventoryDto addInventory(InventoryDto dto) {
        Ingredient ingredient = inventoryDataAccess.findIngredientById(dto.getIngredientId());

        Inventory inventory = new Inventory();
        inventory.setIngredient(ingredient);
        inventory.setQuantity(dto.getQuantity());
        inventory.setReservedQuantity(dto.getReservedQuantity() != null ? dto.getReservedQuantity() : 0);
        inventory.setPricePerUnit(dto.getPricePerUnit());
        inventory.setExpiryDate(dto.getExpiryDate());
        inventory.setReceivedDate(dto.getReceivedDate() != null ? dto.getReceivedDate() : LocalDate.now());

        return toDto(inventoryDataAccess.saveInventory(inventory));
    }

    @Transactional
    public void reserveIngredientsForOrder(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getDish().getIngredients() != null) {
                for (Ingredient ingredient : item.getDish().getIngredients()) {
                    Integer requiredQuantity = item.getQuantity();
                    List<Inventory> availableInventories = inventoryDataAccess.findAvailableForReservation(
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
                        inventoryDataAccess.saveInventory(inventory);
                    }

                    if (remainingQuantity > 0) {
                        throw new BusinessException("Insufficient inventory for ingredient: " + ingredient.getName());
                    }
                }
            }
        }
    }

    @Transactional
    public void consumeReservedIngredients(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getDish().getIngredients() != null) {
                for (Ingredient ingredient : item.getDish().getIngredients()) {
                    Integer requiredQuantity = item.getQuantity();
                    List<Inventory> inventories = inventoryDataAccess.findByIngredientId(ingredient.getId());

                    int remainingQuantity = requiredQuantity;
                    for (Inventory inventory : inventories) {
                        if (remainingQuantity <= 0) break;
                        if (inventory.getReservedQuantity() <= 0) continue;

                        int toConsume = Math.min(remainingQuantity, inventory.getReservedQuantity());
                        inventory.setReservedQuantity(inventory.getReservedQuantity() - toConsume);
                        inventory.setQuantity(inventory.getQuantity() - toConsume);
                        remainingQuantity -= toConsume;
                        inventoryDataAccess.saveInventory(inventory);
                    }
                }
            }
        }
    }

    public InventoryDto getInventoryById(Long id) {
        Inventory inventory = inventoryDataAccess.findInventoryById(id);
        return toDto(inventory);
    }

    public Page<InventoryDto> getAllInventory(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return inventoryDataAccess.findAllInventory(pageable).map(this::toDto);
    }

    public List<InventoryDto> getExpiringInventory(LocalDate date) {
        return inventoryDataAccess.findExpiringSoon(date).stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public InventoryDto updateInventory(Long id, InventoryDto dto) {
        Inventory inventory = inventoryDataAccess.findInventoryById(id);

        inventory.setQuantity(dto.getQuantity());
        inventory.setReservedQuantity(dto.getReservedQuantity());
        if (dto.getPricePerUnit() != null) {
            inventory.setPricePerUnit(dto.getPricePerUnit());
        }
        inventory.setExpiryDate(dto.getExpiryDate());
        
        return toDto(inventoryDataAccess.saveInventory(inventory));
    }

    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryDataAccess.findInventoryById(id);
        inventoryDataAccess.deleteInventory(inventory);
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

