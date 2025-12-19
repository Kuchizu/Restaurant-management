package ru.ifmo.se.restaurant.menu.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ifmo.se.restaurant.menu.dto.CategoryDto;
import ru.ifmo.se.restaurant.menu.dto.DishDto;
import ru.ifmo.se.restaurant.menu.dto.IngredientDto;
import ru.ifmo.se.restaurant.menu.service.MenuService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MenuService menuService;

    @Test
    void createCategory() {
        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        when(menuService.createCategory(any())).thenReturn(Mono.just(dto));
        webTestClient.post().uri("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Test\"}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void getCategoryById() {
        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        when(menuService.getCategoryById(1L)).thenReturn(Mono.just(dto));
        webTestClient.get().uri("/api/categories/1").exchange().expectStatus().isOk();
    }

    @Test
    void getAllCategories() {
        when(menuService.getAllCategories()).thenReturn(Flux.empty());
        webTestClient.get().uri("/api/categories").exchange().expectStatus().isOk();
    }

    @Test
    void createDish() {
        DishDto dto = new DishDto();
        dto.setId(1L);
        when(menuService.createDish(any())).thenReturn(Mono.just(dto));
        webTestClient.post().uri("/api/dishes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Test\",\"price\":10.0,\"categoryId\":1}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void getDishById() {
        DishDto dto = new DishDto();
        dto.setId(1L);
        when(menuService.getDishById(1L)).thenReturn(Mono.just(dto));
        webTestClient.get().uri("/api/dishes/1").exchange().expectStatus().isOk();
    }

    @Test
    void getActiveDishes() {
        when(menuService.getActiveDishes()).thenReturn(Flux.empty());
        webTestClient.get().uri("/api/dishes/active").exchange().expectStatus().isOk();
    }

    @Test
    void createIngredient() {
        IngredientDto dto = new IngredientDto();
        dto.setId(1L);
        when(menuService.createIngredient(any())).thenReturn(Mono.just(dto));
        webTestClient.post().uri("/api/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Test\"}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void getIngredientById() {
        IngredientDto dto = new IngredientDto();
        dto.setId(1L);
        when(menuService.getIngredientById(1L)).thenReturn(Mono.just(dto));
        webTestClient.get().uri("/api/ingredients/1").exchange().expectStatus().isOk();
    }

    @Test
    void getAllIngredients() {
        when(menuService.getAllIngredients()).thenReturn(Flux.empty());
        webTestClient.get().uri("/api/ingredients").exchange().expectStatus().isOk();
    }
}
