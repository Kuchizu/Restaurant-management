package ru.ifmo.se.restaurant.order.application.port.in;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.order.application.dto.EmployeeDto;

public interface ManageEmployeeUseCase {
    Mono<EmployeeDto> createEmployee(EmployeeDto dto);
    Mono<Page<EmployeeDto>> getAllEmployeesPaginated(int page, int size);
}
