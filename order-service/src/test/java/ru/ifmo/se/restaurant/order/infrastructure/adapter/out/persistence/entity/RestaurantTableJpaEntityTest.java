package ru.ifmo.se.restaurant.order.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.order.domain.valueobject.TableStatus;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantTableJpaEntityTest {

    @Test
    void constructor_ShouldSetAllFields() {
        RestaurantTableJpaEntity entity = new RestaurantTableJpaEntity(
                1L, "T1", 4, "Main Hall", TableStatus.FREE
        );

        assertEquals(1L, entity.getId());
        assertEquals("T1", entity.getTableNumber());
        assertEquals(4, entity.getCapacity());
        assertEquals("Main Hall", entity.getLocation());
        assertEquals(TableStatus.FREE, entity.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        RestaurantTableJpaEntity entity = new RestaurantTableJpaEntity();
        entity.setId(1L);
        entity.setTableNumber("VIP-1");
        entity.setCapacity(8);
        entity.setLocation("VIP Section");
        entity.setStatus(TableStatus.OCCUPIED);

        assertEquals(1L, entity.getId());
        assertEquals("VIP-1", entity.getTableNumber());
        assertEquals(8, entity.getCapacity());
        assertEquals("VIP Section", entity.getLocation());
        assertEquals(TableStatus.OCCUPIED, entity.getStatus());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyEntity() {
        RestaurantTableJpaEntity entity = new RestaurantTableJpaEntity();
        assertNull(entity.getId());
        assertNull(entity.getTableNumber());
        assertNull(entity.getCapacity());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        RestaurantTableJpaEntity entity1 = new RestaurantTableJpaEntity(
                1L, "T1", 4, "Main Hall", TableStatus.FREE
        );
        RestaurantTableJpaEntity entity2 = new RestaurantTableJpaEntity(
                1L, "T1", 4, "Main Hall", TableStatus.FREE
        );

        assertEquals(entity1, entity2);
    }
}
