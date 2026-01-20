package ru.ifmo.se.restaurant.inventory.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.application.dto.InventoryDto;

import java.math.BigDecimal;
import java.util.List;

public interface ManageInventoryUseCase {
    List<InventoryDto> getAllInventory();
    Page<InventoryDto> getAllInventoryPaginated(int page, int size);
    Slice<InventoryDto> getAllInventorySlice(int page, int size);
    InventoryDto getInventoryById(Long id);
    List<InventoryDto> getLowStockInventory();
    InventoryDto createInventory(InventoryDto dto);
    InventoryDto updateInventory(Long id, InventoryDto dto);
    InventoryDto adjustInventory(Long id, BigDecimal quantity);
    void deleteInventory(Long id);
}
