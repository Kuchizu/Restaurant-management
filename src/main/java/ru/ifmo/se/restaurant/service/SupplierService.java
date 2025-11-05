package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.InventoryDto;
import ru.ifmo.se.restaurant.dto.SupplierDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderIngredientDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.*;
import ru.ifmo.se.restaurant.model.SupplyOrderStatus;
import ru.ifmo.se.restaurant.repository.IngredientRepository;
import ru.ifmo.se.restaurant.repository.SupplierRepository;
import ru.ifmo.se.restaurant.repository.SupplyOrderIngredientRepository;
import ru.ifmo.se.restaurant.repository.SupplyOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplyOrderIngredientRepository supplyOrderIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryService inventoryService;

    public SupplierService(SupplierRepository supplierRepository,
                          SupplyOrderRepository supplyOrderRepository,
                          SupplyOrderIngredientRepository supplyOrderIngredientRepository,
                          IngredientRepository ingredientRepository,
                          InventoryService inventoryService) {
        this.supplierRepository = supplierRepository;
        this.supplyOrderRepository = supplyOrderRepository;
        this.supplyOrderIngredientRepository = supplyOrderIngredientRepository;
        this.ingredientRepository = ingredientRepository;
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
        return toSupplierDto(supplierRepository.save(supplier));
    }

    public SupplierDto getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return toSupplierDto(supplier);
    }

    public Page<SupplierDto> getAllSuppliers(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return supplierRepository.findByIsActiveTrue(pageable)
            .map(this::toSupplierDto);
    }

    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplier.setName(dto.getName());
        supplier.setAddress(dto.getAddress());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setNotes(dto.getNotes());
        return toSupplierDto(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    @Transactional
    public SupplyOrderDto createSupplyOrder(SupplyOrderDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        SupplyOrder supplyOrder = new SupplyOrder();
        supplyOrder.setSupplier(supplier);
        supplyOrder.setStatus(SupplyOrderStatus.CREATED);
        supplyOrder.setCreatedAt(LocalDateTime.now());
        supplyOrder.setNotes(dto.getNotes());
        supplyOrder.setIngredients(new ArrayList<>());

        SupplyOrder saved = supplyOrderRepository.save(supplyOrder);

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (SupplyOrderIngredientDto ingredientDto : dto.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(ingredientDto.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + ingredientDto.getIngredientId()));

                SupplyOrderIngredient orderIngredient = new SupplyOrderIngredient();
                orderIngredient.setSupplyOrder(saved);
                orderIngredient.setIngredient(ingredient);
                orderIngredient.setQuantity(ingredientDto.getQuantity());
                orderIngredient.setPricePerUnit(ingredientDto.getPricePerUnit());

                supplyOrderIngredientRepository.save(orderIngredient);
                saved.getIngredients().add(orderIngredient);
            }
        }

        return toSupplyOrderDto(saved);
    }

    @Transactional
    public SupplyOrderDto receiveSupplyOrder(Long orderId) {
        SupplyOrder supplyOrder = supplyOrderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Supply order not found with id: " + orderId));

        if (supplyOrder.getStatus() != SupplyOrderStatus.IN_TRANSIT && 
            supplyOrder.getStatus() != SupplyOrderStatus.ORDERED) {
            throw new RuntimeException("Supply order cannot be received with status: " + supplyOrder.getStatus());
        }

        supplyOrder.setStatus(SupplyOrderStatus.RECEIVED);
        supplyOrder.setReceivedAt(LocalDateTime.now());
        supplyOrder = supplyOrderRepository.save(supplyOrder);

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

    public SupplyOrderDto getSupplyOrderById(Long id) {
        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supply order not found with id: " + id));
        return toSupplyOrderDto(supplyOrder);
    }

    public Page<SupplyOrderDto> getAllSupplyOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return supplyOrderRepository.findAll(pageable).map(this::toSupplyOrderDto);
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

