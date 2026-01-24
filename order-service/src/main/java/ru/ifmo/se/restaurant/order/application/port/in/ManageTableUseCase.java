package ru.ifmo.se.restaurant.order.application.port.in;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.dto.TableDto;

public interface ManageTableUseCase {
    Mono<TableDto> createTable(TableDto dto);
    Mono<Page<TableDto>> getAllTablesPaginated(int page, int size);
}
