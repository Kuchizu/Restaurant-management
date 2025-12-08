package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.model.entity.Supplier;
import ru.ifmo.se.restaurant.model.entity.SupplyOrder;
import ru.ifmo.se.restaurant.model.entity.SupplyOrderIngredient;
import ru.ifmo.se.restaurant.repository.IngredientRepository;
import ru.ifmo.se.restaurant.repository.SupplierRepository;
import ru.ifmo.se.restaurant.repository.SupplyOrderIngredientRepository;
import ru.ifmo.se.restaurant.repository.SupplyOrderRepository;

@Component
public class SupplierDataAccess {
    private final SupplierRepository supplierRepository;
    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplyOrderIngredientRepository supplyOrderIngredientRepository;
    private final IngredientRepository ingredientRepository;

    public SupplierDataAccess(SupplierRepository supplierRepository,
                             SupplyOrderRepository supplyOrderRepository,
                             SupplyOrderIngredientRepository supplyOrderIngredientRepository,
                             IngredientRepository ingredientRepository) {
        this.supplierRepository = supplierRepository;
        this.supplyOrderRepository = supplyOrderRepository;
        this.supplyOrderIngredientRepository = supplyOrderIngredientRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Page<Supplier> findAllSuppliersByIsActiveTrue(Pageable pageable) {
        return supplierRepository.findByIsActiveTrue(pageable);
    }

    public Ingredient findIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found with id: " + id));
    }

    public SupplyOrder saveSupplyOrder(SupplyOrder order) {
        return supplyOrderRepository.save(order);
    }

    public SupplyOrder findSupplyOrderById(Long id) {
        return supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supply order not found with id: " + id));
    }

    public Page<SupplyOrder> findAllSupplyOrders(Pageable pageable) {
        return supplyOrderRepository.findAll(pageable);
    }

    public SupplyOrderIngredient saveSupplyOrderIngredient(SupplyOrderIngredient ingr) {
        return supplyOrderIngredientRepository.save(ingr);
    }

    public void deleteSupplier(Supplier supplier) {
        supplierRepository.delete(supplier);
    }
}
