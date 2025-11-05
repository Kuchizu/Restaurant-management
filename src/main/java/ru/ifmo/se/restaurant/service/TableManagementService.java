package ru.ifmo.se.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.dto.TableDto;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.entity.Table;
import ru.ifmo.se.restaurant.model.TableStatus;
import ru.ifmo.se.restaurant.repository.TableRepository;

@Service
public class TableManagementService {
    private final TableRepository tableRepository;

    public TableManagementService(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Transactional
    public TableDto createTable(TableDto dto) {
        Table table = new Table();
        table.setTableNumber(dto.getTableNumber());
        table.setCapacity(dto.getCapacity());
        table.setLocation(dto.getLocation());
        table.setStatus(dto.getStatus() != null ? dto.getStatus() : TableStatus.FREE);
        table.setIsActive(true);
        return toDto(tableRepository.save(table));
    }

    public TableDto getTableById(@NonNull Long id) {
        Table table = tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        return toDto(table);
    }

    public Page<TableDto> getAllTables(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return tableRepository.findByIsActiveTrue(pageable)
            .map(this::toDto);
    }

    public Page<TableDto> getTablesByStatus(TableStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return tableRepository.findByStatusAndIsActiveTrue(status, pageable)
            .map(this::toDto);
    }

    @Transactional
    public TableDto updateTable(@NonNull Long id, TableDto dto) {
        Table table = tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        table.setTableNumber(dto.getTableNumber());
        table.setCapacity(dto.getCapacity());
        table.setLocation(dto.getLocation());
        if (dto.getStatus() != null) {
            table.setStatus(dto.getStatus());
        }
        return toDto(tableRepository.save(table));
    }

    @Transactional
    public void deleteTable(@NonNull Long id) {
        Table table = tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        table.setIsActive(false);
        tableRepository.save(table);
    }

    private TableDto toDto(Table table) {
        TableDto dto = new TableDto();
        dto.setId(table.getId());
        dto.setTableNumber(table.getTableNumber());
        dto.setCapacity(table.getCapacity());
        dto.setLocation(table.getLocation());
        dto.setStatus(table.getStatus());
        dto.setIsActive(table.getIsActive());
        return dto;
    }
}

