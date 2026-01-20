package ru.ifmo.se.restaurant.menu.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.menu.application.dto.DishDto;

public interface ManageDishesUseCase {
    Mono<DishDto> createDish(DishDto dto);
    Mono<DishDto> getDishById(Long id);
    Mono<DishDto> getDishByName(String name);
    Mono<Page<DishDto>> getAllDishes(Pageable pageable);
    Mono<Page<DishDto>> getAllDishesPaginated(int page, int size);
    Mono<Slice<DishDto>> getAllDishesSlice(int page, int size);
    Flux<DishDto> getActiveDishes();
    Mono<DishDto> updateDish(Long id, DishDto dto);
    Mono<Void> deleteDish(Long id);
}
