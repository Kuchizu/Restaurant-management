package ru.ifmo.se.restaurant.inventory.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplierDto;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplyOrderItemDto;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageSupplierUseCase;
import ru.ifmo.se.restaurant.inventory.application.port.in.ManageSupplyOrderUseCase;
import ru.ifmo.se.restaurant.inventory.application.port.out.IngredientRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.InventoryRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplierRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplyOrderItemRepository;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplyOrderRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.*;
import ru.ifmo.se.restaurant.inventory.domain.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.inventory.domain.exception.ValidationException;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;
import ru.ifmo.se.restaurant.inventory.util.PaginationUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupplierManagementService implements ManageSupplierUseCase, ManageSupplyOrderUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplyOrderItemRepository supplyOrderItemRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryRepository inventoryRepository;

    // Supplier Management

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAll().stream()
            .map(SupplierDto::fromDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDto> getAllSuppliersPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return supplierRepository.findAll(pageable)
            .map(SupplierDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<SupplierDto> getAllSuppliersSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return supplierRepository.findAllSlice(pageable)
            .map(SupplierDto::fromDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDto getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        return SupplierDto.fromDomain(supplier);
    }

    @Override
    public SupplierDto createSupplier(SupplierDto dto) {
        Supplier supplier = Supplier.builder()
            .name(dto.getName())
            .contactPerson(dto.getContactPerson())
            .phone(dto.getPhone())
            .email(dto.getEmail())
            .address(dto.getAddress())
            .build();

        Supplier saved = supplierRepository.save(supplier);
        return SupplierDto.fromDomain(saved);
    }

    @Override
    public SupplierDto updateSupplier(Long id, SupplierDto dto) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        Supplier updated = supplier.updateInfo(
            dto.getName(),
            dto.getContactPerson(),
            dto.getPhone(),
            dto.getEmail(),
            dto.getAddress()
        );

        Supplier saved = supplierRepository.save(updated);
        return SupplierDto.fromDomain(saved);
    }

    @Override
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found");
        }
        supplierRepository.deleteById(id);
    }

    // Supply Order Management

    @Override
    @Transactional(readOnly = true)
    public List<SupplyOrderDto> getAllSupplyOrders() {
        return supplyOrderRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplyOrderDto> getAllSupplyOrdersPaginated(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        return supplyOrderRepository.findAll(pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<SupplyOrderDto> getAllSupplyOrdersSlice(int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        return supplyOrderRepository.findAllSlice(pageable)
            .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplyOrderDto getSupplyOrderById(Long id) {
        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supply order not found"));
        return toDto(supplyOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplyOrderDto> getSupplyOrdersByStatus(SupplyOrderStatus status) {
        return supplyOrderRepository.findByStatus(status).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public SupplyOrderDto createSupplyOrder(SupplyOrderDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));

        // Calculate total cost and create items
        BigDecimal totalCost = BigDecimal.ZERO;
        List<SupplyOrderItem> items = new ArrayList<>();

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (SupplyOrderItemDto itemDto : dto.getItems()) {
                Ingredient ingredient = ingredientRepository.findById(itemDto.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

                SupplyOrderItem item = SupplyOrderItem.builder()
                    .ingredient(ingredient)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();

                items.add(item);

                if (itemDto.getUnitPrice() != null && itemDto.getQuantity() != null) {
                    totalCost = totalCost.add(item.getTotalPrice());
                }
            }
        }

        SupplyOrder supplyOrder = SupplyOrder.builder()
            .supplier(supplier)
            .orderDate(LocalDateTime.now())
            .status(SupplyOrderStatus.PENDING)
            .totalCost(totalCost)
            .notes(dto.getNotes())
            .items(items)
            .build();

        SupplyOrder saved = supplyOrderRepository.save(supplyOrder);

        // Save items
        for (SupplyOrderItem item : items) {
            supplyOrderItemRepository.save(item);
        }

        return toDto(saved);
    }

    @Override
    public SupplyOrderDto updateSupplyOrderStatus(Long id, SupplyOrderStatus status) {
        SupplyOrder supplyOrder = supplyOrderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supply order not found"));

        SupplyOrder updated = supplyOrder.updateStatus(status);

        // If delivered, update inventory
        if (status == SupplyOrderStatus.DELIVERED) {
            List<SupplyOrderItem> items = supplyOrderItemRepository.findBySupplyOrderId(id);
            for (SupplyOrderItem item : items) {
                inventoryRepository.findByIngredientId(item.getIngredient().getId())
                    .ifPresent(inventory -> {
                        Inventory adjusted = inventory.adjustQuantity(item.getQuantity());
                        inventoryRepository.save(adjusted);
                    });
            }
        }

        SupplyOrder saved = supplyOrderRepository.save(updated);
        return toDto(saved);
    }

    @Override
    public void deleteSupplyOrder(Long id) {
        if (!supplyOrderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supply order not found");
        }
        supplyOrderRepository.deleteById(id);
    }

    private SupplyOrderDto toDto(SupplyOrder supplyOrder) {
        if (supplyOrder.getSupplier() == null) {
            log.error("SupplyOrder {} has null supplier - data integrity issue", supplyOrder.getId());
            throw new ValidationException(
                "Supply order has no supplier assigned",
                "supplier",
                null
            );
        }

        List<SupplyOrderItem> items = supplyOrderItemRepository.findBySupplyOrderId(supplyOrder.getId());

        return SupplyOrderDto.builder()
            .id(supplyOrder.getId())
            .supplierId(supplyOrder.getSupplier().getId())
            .supplierName(supplyOrder.getSupplier().getName())
            .orderDate(supplyOrder.getOrderDate())
            .deliveryDate(supplyOrder.getDeliveryDate())
            .status(supplyOrder.getStatus())
            .totalCost(supplyOrder.getTotalCost())
            .notes(supplyOrder.getNotes())
            .items(items.stream()
                .map(item -> {
                    if (item.getIngredient() == null) {
                        log.error("SupplyOrderItem {} has null ingredient - data integrity issue", item.getId());
                        throw new ValidationException(
                            "Supply order item has no ingredient assigned",
                            "ingredient",
                            null
                        );
                    }
                    return SupplyOrderItemDto.fromDomain(item);
                })
                .collect(Collectors.toList()))
            .build();
    }
}
