package ru.ifmo.se.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.dto.IngredientDto;
import ru.ifmo.se.restaurant.mapper.CategoryMapper;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Ingredient Management", description = "API for managing ingredients")
public class IngredientController {
    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new ingredient")
    public ResponseEntity<IngredientDto> createIngredient(@Valid @RequestBody IngredientDto dto) {
        ru.ifmo.se.restaurant.model.entity.Ingredient ingredient = new ru.ifmo.se.restaurant.model.entity.Ingredient();
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        ingredient = ingredientRepository.save(ingredient);
        return new ResponseEntity<>(toDto(ingredient), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get ingredient by ID")
    public ResponseEntity<IngredientDto> getIngredient(@PathVariable Long id) {
        ru.ifmo.se.restaurant.model.entity.Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ru.ifmo.se.restaurant.exception.ResourceNotFoundException("Ingredient not found with id: " + id));
        return ResponseEntity.ok(toDto(ingredient));
    }

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get all ingredients with pagination")
    public ResponseEntity<Page<IngredientDto>> getAllIngredients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IngredientDto> result = ingredientRepository.findAll(
            org.springframework.data.domain.PageRequest.of(page, Math.min(size, 50)))
            .map(this::toDto);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(result.getTotalElements()))
            .body(result);
    }

    private IngredientDto toDto(ru.ifmo.se.restaurant.model.entity.Ingredient ingredient) {
        IngredientDto dto = new IngredientDto();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());
        dto.setUnit(ingredient.getUnit());
        return dto;
    }
}

