package ru.ifmo.se.restaurant.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.TableDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.TableStatus;

import static org.junit.jupiter.api.Assertions.*;

class TableManagementServiceTest extends BaseIntegrationTest {
    @Autowired
    private TableManagementService tableService;

    @Test
    void testCreateAndGetTable() {
        TableDto table = new TableDto();
        table.setTableNumber(10);
        table.setCapacity(4);
        table.setLocation("Window");
        table.setStatus(TableStatus.FREE);

        TableDto created = tableService.createTable(table);
        assertNotNull(created.getId());
        assertEquals(10, created.getTableNumber());

        TableDto found = tableService.getTableById(created.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void testGetAllTables() {
        TableDto table1 = new TableDto();
        table1.setTableNumber(11);
        table1.setCapacity(2);
        table1.setLocation("Center");
        tableService.createTable(table1);

        TableDto table2 = new TableDto();
        table2.setTableNumber(12);
        table2.setCapacity(6);
        table2.setLocation("VIP");
        tableService.createTable(table2);

        Page<TableDto> tables = tableService.getAllTables(0, 10);
        assertTrue(tables.getTotalElements() >= 2);
    }

    @Test
    void testGetTablesByStatus() {
        TableDto table = new TableDto();
        table.setTableNumber(13);
        table.setCapacity(4);
        table.setLocation("Window");
        table.setStatus(TableStatus.OCCUPIED);
        tableService.createTable(table);

        Page<TableDto> occupiedTables = tableService.getTablesByStatus(TableStatus.OCCUPIED, 0, 10);
        assertTrue(occupiedTables.getTotalElements() >= 1);
    }

    @Test
    void testUpdateTable() {
        TableDto table = new TableDto();
        table.setTableNumber(14);
        table.setCapacity(2);
        table.setLocation("Original");
        TableDto created = tableService.createTable(table);

        created.setCapacity(8);
        created.setLocation("Updated");
        TableDto updated = tableService.updateTable(created.getId(), created);
        assertEquals(8, updated.getCapacity());
        assertEquals("Updated", updated.getLocation());
    }

    @Test
    void testDeleteTable() {
        TableDto table = new TableDto();
        table.setTableNumber(15);
        table.setCapacity(4);
        table.setLocation("To Delete");
        TableDto created = tableService.createTable(table);

        tableService.deleteTable(created.getId());
        // Table should be inactive but still retrievable
        assertDoesNotThrow(() -> tableService.getTableById(created.getId()));
    }
}

