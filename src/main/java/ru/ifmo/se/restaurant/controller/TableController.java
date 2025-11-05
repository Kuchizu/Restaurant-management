package ru.ifmo.se.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.TableDto;
import ru.ifmo.se.restaurant.model.TableStatus;
import ru.ifmo.se.restaurant.service.TableManagementService;

@RestController
@RequestMapping("/api/tables")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Table Management", description = "API for managing restaurant tables")
public class TableController {
    private final TableManagementService tableService;

    public TableController(TableManagementService tableService) {
        this.tableService = tableService;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new table")
    public ResponseEntity<TableDto> createTable(@Valid @RequestBody TableDto dto) {
        return new ResponseEntity<>(tableService.createTable(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get table by ID")
    public ResponseEntity<TableDto> getTable(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getTableById(id));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all tables with pagination")
    public ResponseEntity<Page<TableDto>> getAllTables(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TableDto> result = tableService.getAllTables(page, size);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(result.getTotalElements()))
            .body(result);
    }

    @GetMapping("/status/{status}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get tables by status")
    public ResponseEntity<Page<TableDto>> getTablesByStatus(
            @PathVariable TableStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(tableService.getTablesByStatus(status, page, size));
    }

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update table")
    public ResponseEntity<TableDto> updateTable(@PathVariable Long id, @Valid @RequestBody TableDto dto) {
        return ResponseEntity.ok(tableService.updateTable(id, dto));
    }

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete table")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.noContent().build();
    }
}

