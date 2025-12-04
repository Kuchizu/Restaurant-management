package ru.ifmo.se.restaurant.dataaccess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.model.entity.Table;
import ru.ifmo.se.restaurant.model.TableStatus;
import ru.ifmo.se.restaurant.repository.TableRepository;
import java.util.Optional;

@Component
public class TableManagementDataAccess {
    private final TableRepository tableRepository;
    public TableManagementDataAccess(TableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }
    public Table saveTable(Table table) { return tableRepository.save(table); }
    public Optional<Table> findTableById(Long id) { return tableRepository.findById(id); }
    public Page<Table> findActiveTables(Pageable pageable) { return tableRepository.findByIsActiveTrue(pageable); }
    public Page<Table> findTablesByStatusAndActive(TableStatus status, Pageable pageable) { return tableRepository.findByStatusAndIsActiveTrue(status, pageable); }
}
