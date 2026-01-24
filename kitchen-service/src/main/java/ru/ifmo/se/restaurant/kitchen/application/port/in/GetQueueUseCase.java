package ru.ifmo.se.restaurant.kitchen.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;

import java.util.List;

public interface GetQueueUseCase {
    List<KitchenQueueDto> getActiveQueue();
    List<KitchenQueueDto> getAllQueue();
    KitchenQueueDto getQueueItemById(Long id);
    List<KitchenQueueDto> getQueueByOrderId(Long orderId);
    Page<KitchenQueueDto> getAllQueueItemsPaginated(int page, int size);
    Slice<KitchenQueueDto> getAllQueueItemsSlice(int page, int size);
}
