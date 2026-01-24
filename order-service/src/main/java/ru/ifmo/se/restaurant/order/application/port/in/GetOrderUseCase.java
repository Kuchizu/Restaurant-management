package ru.ifmo.se.restaurant.order.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.dto.OrderDto;

public interface GetOrderUseCase {
    Mono<OrderDto> getOrderById(Long id);
    Flux<OrderDto> getAllOrders();
    Mono<Page<OrderDto>> getAllOrdersPaginated(int page, int size);
    Mono<Slice<OrderDto>> getAllOrdersSlice(int page, int size);
}
