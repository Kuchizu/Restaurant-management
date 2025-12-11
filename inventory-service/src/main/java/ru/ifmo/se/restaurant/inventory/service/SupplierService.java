package ru.ifmo.se.restaurant.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.inventory.dto.SupplierDto;
import ru.ifmo.se.restaurant.inventory.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.dto.SupplyOrderItemDto;
import ru.ifmo.se.restaurant.inventory.entity.*;
import ru.ifmo.se.restaurant.inventory.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.exception.ValidationException;
import ru.ifmo.se.restaurant.inventory.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplyOrderIngredientRepository supplyOrderIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryRepository inventoryRepository;

    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public SupplierDto getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());

        return toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        if (dto.getName() != null) {
            supplier.setName(dto.getName());
        }
        if (dto.getContactPerson() != null) {
            supplier.setContactPerson(dto.getContactPerson());
        }
        if (dto.getPhone() != null) {
            supplier.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            supplier.setEmail(dto.getEmail());
        }
        if (dto.getAddress() != null) {
            supplier.setAddress(dto.getAddress());
        }

        return toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found");
        }
        supplierRepository.deleteById(id);
    }

    public List<SupplyOrderDto> getAllSupplyOrders() {
        return supplyOrderRepository.findAll().stream()
                .map(this::toSupplyOrderDto)
                .collect(Collectors.toList());
    }

    public SupplyOrderDto getSupplyOrderById(Long id) {
        return supplyOrderRepository.findById(id)
                .map(this::toSupplyOrderDto)
                .orElseThrow(() -> new ResourceNotFoundException("Supply order not found"));
    }

    public List<SupplyOrderDto> getSupplyOrdersByStatus(SupplyOrderStatus status) {
        return supplyOrderRepository.findByStatus(status).stream()
                .map(this::toSupplyOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplyOrderDto createSupplyOrder(SupplyOrderDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        SupplyOrder supplyOrder = new SupplyOrder();
        supplyOrder.setSupplier(supplier);
        supplyOrder.setOrderDate(LocalDateTime.now());
        supplyOrder.setStatus(SupplyOrderStatus.PENDING);
        supplyOrder.setNotes(dto.getNotes());

        SupplyOrder savedOrder = supplyOrderRepository.save(supplyOrder);

        BigDecimal totalCost = BigDecimal.ZERO;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (SupplyOrderItemDto itemDto : dto.getItems()) {
                Ingredient ingredient = ingredientRepository.findById(itemDto.getIngredientId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

                SupplyOrderIngredient item = new SupplyOrderIngredient();
                item.setSupplyOrder(savedOrder);
                item.setIngredient(ingredient);
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                supplyOrderIngredientRepository.save(item);

                if (itemDto.getUnitPrice() != null && itemDto.getQuantity() != null) {
                    totalCost = totalCost.add(itemDto.getUnitPrice().multiply(itemDto.getQuantity()));
                }
            }
        }

        savedOrder.setTotalCost(totalCost);
        return toSupplyOrderDto(supplyOrderRepository.save(savedOrder));
    }

    @Transactional
    public SupplyOrderDto updateSupplyOrderStatus(Long id, SupplyOrderStatus status) {
        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supply order not found"));

        supplyOrder.setStatus(status);

        if (status == SupplyOrderStatus.DELIVERED) {
            supplyOrder.setDeliveryDate(LocalDateTime.now());
            List<SupplyOrderIngredient> items = supplyOrderIngredientRepository.findBySupplyOrderId(id);
            for (SupplyOrderIngredient item : items) {
                inventoryRepository.findByIngredientId(item.getIngredient().getId())
                        .ifPresent(inventory -> {
                            inventory.setQuantity(inventory.getQuantity().add(item.getQuantity()));
                            inventory.setLastUpdated(LocalDateTime.now());
                            inventoryRepository.save(inventory);
                        });
            }
        }

        return toSupplyOrderDto(supplyOrderRepository.save(supplyOrder));
    }

    @Transactional
    public void deleteSupplyOrder(Long id) {
        if (!supplyOrderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supply order not found");
        }
        supplyOrderRepository.deleteById(id);
    }

    private SupplierDto toDto(Supplier supplier) {
        SupplierDto dto = new SupplierDto();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setPhone(supplier.getPhone());
        dto.setEmail(supplier.getEmail());
        dto.setAddress(supplier.getAddress());
        return dto;
    }

    private SupplyOrderDto toSupplyOrderDto(SupplyOrder supplyOrder) {
        if (supplyOrder.getSupplier() == null) {
            log.error("SupplyOrder {} has null supplier - data integrity issue", supplyOrder.getId());
            throw new ValidationException(
                "Supply order has no supplier assigned",
                "supplier",
                null
            );
        }
        SupplyOrderDto dto = new SupplyOrderDto();
        dto.setId(supplyOrder.getId());
        dto.setSupplierId(supplyOrder.getSupplier().getId());
        dto.setSupplierName(supplyOrder.getSupplier().getName());
        dto.setOrderDate(supplyOrder.getOrderDate());
        dto.setDeliveryDate(supplyOrder.getDeliveryDate());
        dto.setStatus(supplyOrder.getStatus());
        dto.setTotalCost(supplyOrder.getTotalCost());
        dto.setNotes(supplyOrder.getNotes());

        List<SupplyOrderItemDto> items = supplyOrderIngredientRepository
                .findBySupplyOrderId(supplyOrder.getId()).stream()
                .map(this::toSupplyOrderItemDto)
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }

    private SupplyOrderItemDto toSupplyOrderItemDto(SupplyOrderIngredient item) {
        if (item.getIngredient() == null) {
            log.error("SupplyOrderIngredient {} has null ingredient - data integrity issue", item.getId());
            throw new ValidationException(
                "Supply order item has no ingredient assigned",
                "ingredient",
                null
            );
        }
        SupplyOrderItemDto dto = new SupplyOrderItemDto();
        dto.setId(item.getId());
        dto.setIngredientId(item.getIngredient().getId());
        dto.setIngredientName(item.getIngredient().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        return dto;
    }
}
