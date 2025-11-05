package ru.ifmo.se.restaurant.mapper;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public interface Mapper<E, D> {
    D toDto(E entity);
    E toEntity(D dto);

    default List<D> toDtoList(List<E> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    default Page<D> toDtoPage(Page<E> entityPage) {
        return entityPage.map(this::toDto);
    }
}

