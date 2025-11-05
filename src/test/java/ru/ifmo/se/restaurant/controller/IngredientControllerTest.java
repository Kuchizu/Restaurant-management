package ru.ifmo.se.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.IngredientDto;
import ru.ifmo.se.restaurant.model.entity.Ingredient;
import ru.ifmo.se.restaurant.repository.IngredientRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class IngredientControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long ingredientId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Salt");
        ingredient.setUnit("kg");
        Ingredient saved = ingredientRepository.save(ingredient);
        ingredientId = saved.getId();
    }

    @Test
    void testCreateIngredient() throws Exception {
        IngredientDto ingredient = new IngredientDto();
        ingredient.setName("Pepper");
        ingredient.setUnit("kg");

        mockMvc.perform(post("/api/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ingredient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Pepper"));
    }

    @Test
    void testGetIngredient() throws Exception {
        mockMvc.perform(get("/api/ingredients/" + ingredientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ingredientId));
    }

    @Test
    void testGetAllIngredients() throws Exception {
        mockMvc.perform(get("/api/ingredients")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(header().exists("X-Total-Count"));
    }
}

