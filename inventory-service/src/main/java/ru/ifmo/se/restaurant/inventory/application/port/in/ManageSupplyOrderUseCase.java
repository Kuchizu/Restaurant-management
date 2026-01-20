package ru.ifmo.se.restaurant.inventory.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.inventory.domain.valueobject.SupplyOrderStatus;

import java.util.List;

public interface ManageSupplyOrderUseCase {
    List<SupplyOrderDto> getAllSupplyOrders();
    Page<SupplyOrderDto> getAllSupplyOrdersPaginated(int page, int size);
    Slice<SupplyOrderDto> getAllSupplyOrdersSlice(int page, int size);
    SupplyOrderDto getSupplyOrderById(Long id);
    List<SupplyOrderDto> getSupplyOrdersByStatus(SupplyOrderStatus status);
    SupplyOrderDto createSupplyOrder(SupplyOrderDto dto);
    SupplyOrderDto updateSupplyOrderStatus(Long id, SupplyOrderStatus status);
    void deleteSupplyOrder(Long id);
}
