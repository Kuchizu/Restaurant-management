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
        assertDoesNotThrow(() -> tableService.getTableById(created.getId()));
    }

    @Test
    void testGetTableByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            tableService.getTableById(99999L));
    }

    @Test
    void testUpdateTableNotFound() {
        TableDto dto = new TableDto();
        dto.setTableNumber(99);
        dto.setCapacity(4);
        assertThrows(ResourceNotFoundException.class, () ->
            tableService.updateTable(99999L, dto));
    }

    @Test
    void testDeleteTableNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
            tableService.deleteTable(99999L));
    }

    @Test
    void testGetTablesByStatusFree() {
        TableDto table = new TableDto();
        table.setTableNumber(16);
        table.setCapacity(2);
        table.setLocation("Corner");
        table.setStatus(TableStatus.FREE);
        tableService.createTable(table);

        Page<TableDto> freeTables = tableService.getTablesByStatus(TableStatus.FREE, 0, 10);
        assertTrue(freeTables.getTotalElements() >= 1);
    }

    @Test
    void testCreateTableWithDefaultStatus() {
        TableDto table = new TableDto();
        table.setTableNumber(17);
        table.setCapacity(4);
        table.setLocation("Center");

        TableDto created = tableService.createTable(table);
        assertNotNull(created.getId());
        assertEquals(17, created.getTableNumber());
    }

    @Test
    void testUpdateTableStatus() {
        TableDto table = new TableDto();
        table.setTableNumber(18);
        table.setCapacity(4);
        table.setLocation("Window");
        table.setStatus(TableStatus.FREE);
        TableDto created = tableService.createTable(table);

        created.setStatus(TableStatus.RESERVED);
        TableDto updated = tableService.updateTable(created.getId(), created);
        assertEquals(TableStatus.RESERVED, updated.getStatus());
    }
}

