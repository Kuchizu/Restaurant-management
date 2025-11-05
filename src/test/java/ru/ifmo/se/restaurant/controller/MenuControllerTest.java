package ru.ifmo.se.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ifmo.se.restaurant.BaseIntegrationTest;
import ru.ifmo.se.restaurant.dto.CategoryDto;
import ru.ifmo.se.restaurant.service.MenuManagementService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class MenuControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MenuManagementService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        CategoryDto category = new CategoryDto();
        category.setName("Test Category");
        CategoryDto created = menuService.createCategory(category);
        categoryId = created.getId();
    }

    @Test
    void testCreateCategory() throws Exception {
        CategoryDto category = new CategoryDto();
        category.setName("New Category");
        category.setDescription("Description");

        mockMvc.perform(post("/api/menu/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    void testGetCategory() throws Exception {
        mockMvc.perform(get("/api/menu/categories/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId));
    }

    @Test
    void testGetAllCategories() throws Exception {
        mockMvc.perform(get("/api/menu/categories")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

