package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ifmo.se.restaurant.dataaccess.SupplierDataAccess;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.dto.SupplierDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderIngredientDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.*;
import ru.ifmo.se.restaurant.model.SupplyOrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    private final SupplierDataAccess supplierDataAccess;
    private final InventoryService inventoryService;

    public SupplierService(SupplierDataAccess supplierDataAccess,
                          InventoryService inventoryService) {
        this.supplierDataAccess = supplierDataAccess;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public SupplierDto createSupplier(SupplierDto dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setAddress(dto.getAddress());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setNotes(dto.getNotes());
        supplier.setIsActive(true);
        return toSupplierDto(supplierDataAccess.saveSupplier(supplier));
    }

    public SupplierDto getSupplierById(@NonNull Long id) {
        Supplier supplier = supplierDataAccess.findSupplierById(id);
        return toSupplierDto(supplier);
    }

    public Page<SupplierDto> getAllSuppliers(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return supplierDataAccess.findAllSuppliersByIsActiveTrue(pageable)
            .map(this::toSupplierDto);
    }

    @Transactional
    public SupplierDto updateSupplier(@NonNull Long id, SupplierDto dto) {
        Supplier supplier = supplierDataAccess.findSupplierById(id)
        
        supplier.setName(dto.getName());
        supplier.setAddress(dto.getAddress());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setNotes(dto.getNotes());
        return toSupplierDto(supplierDataAccess.saveSupplier(supplier));
    }

    @Transactional
    public void deleteSupplier(@NonNull Long id) {
        Supplier supplier = supplierDataAccess.findSupplierById(id)
    
        supplier.setIsActive(false);
        supplierDataAccess.saveSupplier(supplier);
    }

    @Transactional
    public SupplyOrderDto createSupplyOrder(SupplyOrderDto dto) {
        Supplier supplier = supplierDataAccess.findSupplierById(dto.getSupplierId());

        SupplyOrder supplyOrder = new SupplyOrder();
        supplyOrder.setSupplier(supplier);
        supplyOrder.setStatus(SupplyOrderStatus.CREATED);
        supplyOrder.setCreatedAt(LocalDateTime.now());
        supplyOrder.setNotes(dto.getNotes());
        supplyOrder.setIngredients(new ArrayList<>());

        SupplyOrder saved = supplierDataAccess.saveSupplyOrder(supplyOrder);

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (SupplyOrderIngredientDto ingredientDto : dto.getIngredients()) {
                Ingredient ingredient = supplierDataAccess.findIngredientById(ingredientDto.getIngredientId());

                SupplyOrderIngredient orderIngredient = new SupplyOrderIngredient();
                orderIngredient.setSupplyOrder(saved);
                orderIngredient.setIngredient(ingredient);
                orderIngredient.setQuantity(ingredientDto.getQuantity());
                orderIngredient.setPricePerUnit(ingredientDto.getPricePerUnit());

                supplierDataAccess.saveSupplyOrderIngredient(orderIngredient);
                saved.getIngredients().add(orderIngredient);
            }
        }

        return toSupplyOrderDto(saved);
    }

    @Transactional
    public SupplyOrderDto receiveSupplyOrder(@NonNull Long orderId) {
        SupplyOrder supplyOrder = supplierDataAccess.findSupplyOrderById(orderId);

        if (supplyOrder.getStatus() != SupplyOrderStatus.IN_TRANSIT && 
            supplyOrder.getStatus() != SupplyOrderStatus.ORDERED) {
            throw new RuntimeException("Supply order cannot be received with status: " + supplyOrder.getStatus());
        }

        supplyOrder.setStatus(SupplyOrderStatus.RECEIVED);
        supplyOrder.setReceivedAt(LocalDateTime.now());
        supplyOrder = supplierDataAccess.saveSupplyOrder(supplyOrder);

        for (SupplyOrderIngredient orderIngredient : supplyOrder.getIngredients()) {
            InventoryDto inventoryDto = new InventoryDto();
            inventoryDto.setIngredientId(orderIngredient.getIngredient().getId());
            inventoryDto.setQuantity(orderIngredient.getQuantity());
            inventoryDto.setReservedQuantity(0);
            inventoryDto.setPricePerUnit(orderIngredient.getPricePerUnit());
            inventoryDto.setExpiryDate(java.time.LocalDate.now().plusDays(30));
            inventoryService.addInventory(inventoryDto);
        }

        return toSupplyOrderDto(supplyOrder);
    }

    public SupplyOrderDto getSupplyOrderById(@NonNull Long id) {
        SupplyOrder supplyOrder = supplierDataAccess.findSupplyOrderById(id);
        return toSupplyOrderDto(supplyOrder);
    }

    public Page<SupplyOrderDto> getAllSupplyOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return supplierDataAccess.findAllSupplyOrders(pageable).map(this::toSupplyOrderDto);
    }

    private SupplierDto toSupplierDto(Supplier supplier) {
        SupplierDto dto = new SupplierDto();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setAddress(supplier.getAddress());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setNotes(supplier.getNotes());
        dto.setIsActive(supplier.getIsActive());
        return dto;
    }

    private SupplyOrderDto toSupplyOrderDto(SupplyOrder supplyOrder) {
        SupplyOrderDto dto = new SupplyOrderDto();
        dto.setId(supplyOrder.getId());
        dto.setSupplierId(supplyOrder.getSupplier().getId());
        dto.setSupplierName(supplyOrder.getSupplier().getName());
        dto.setStatus(supplyOrder.getStatus());
        dto.setCreatedAt(supplyOrder.getCreatedAt());
        dto.setOrderedAt(supplyOrder.getOrderedAt());
        dto.setReceivedAt(supplyOrder.getReceivedAt());
        dto.setNotes(supplyOrder.getNotes());
        dto.setIngredients(supplyOrder.getIngredients().stream()
            .map(this::toSupplyOrderIngredientDto)
            .collect(Collectors.toList()));
        return dto;
    }

    private SupplyOrderIngredientDto toSupplyOrderIngredientDto(SupplyOrderIngredient orderIngredient) {
        SupplyOrderIngredientDto dto = new SupplyOrderIngredientDto();
        dto.setId(orderIngredient.getId());
        dto.setIngredientId(orderIngredient.getIngredient().getId());
        dto.setIngredientName(orderIngredient.getIngredient().getName());
        dto.setQuantity(orderIngredient.getQuantity());
        dto.setPricePerUnit(orderIngredient.getPricePerUnit());
        return dto;
    }
}

