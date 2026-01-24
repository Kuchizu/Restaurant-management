package ru.ifmo.se.restaurant.menu.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.menu.application.dto.CategoryDto;

public interface ManageCategoriesUseCase {
    Mono<CategoryDto> createCategory(CategoryDto dto);
    Mono<CategoryDto> getCategoryById(Long id);
    Flux<CategoryDto> getAllCategories();
    Mono<Page<CategoryDto>> getAllCategoriesPaginated(int page, int size);
    Mono<Slice<CategoryDto>> getAllCategoriesSlice(int page, int size);
    Mono<CategoryDto> updateCategory(Long id, CategoryDto dto);
    Mono<Void> deleteCategory(Long id);
}
