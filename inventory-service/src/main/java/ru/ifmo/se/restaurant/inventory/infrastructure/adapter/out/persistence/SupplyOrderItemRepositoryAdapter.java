package ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.inventory.application.port.out.SupplyOrderItemRepository;
import ru.ifmo.se.restaurant.inventory.domain.entity.SupplyOrderItem;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.IngredientJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplyOrderIngredientJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.entity.SupplyOrderJpaEntity;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.IngredientJpaRepository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplyOrderIngredientJpaRepository;
import ru.ifmo.se.restaurant.inventory.infrastructure.adapter.out.persistence.repository.SupplyOrderJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyOrderItemRepositoryAdapter implements SupplyOrderItemRepository {

    private final SupplyOrderIngredientJpaRepository jpaRepository;
    private final SupplyOrderJpaRepository supplyOrderJpaRepository;
    private final IngredientJpaRepository ingredientJpaRepository;

    @Override
    public SupplyOrderItem save(SupplyOrderItem item) {
        log.debug("Saving supply order item: {}", item);

        SupplyOrderJpaEntity orderEntity = supplyOrderJpaRepository.findById(item.getId())
            .orElseThrow(() -> new IllegalArgumentException("Supply order not found"));

        IngredientJpaEntity ingredientEntity = ingredientJpaRepository.findById(item.getIngredient().getId())
            .orElseThrow(() -> new IllegalArgumentException("Ingredient not found"));

        SupplyOrderIngredientJpaEntity entity = SupplyOrderIngredientJpaEntity.fromDomain(item, orderEntity, ingredientEntity);
        SupplyOrderIngredientJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<SupplyOrderItem> findBySupplyOrderId(Long supplyOrderId) {
        log.debug("Finding supply order items by supply order id: {}", supplyOrderId);
        return jpaRepository.findBySupplyOrderId(supplyOrderId).stream()
            .map(SupplyOrderIngredientJpaEntity::toDomain)
            .collect(Collectors.toList());
    }
}
