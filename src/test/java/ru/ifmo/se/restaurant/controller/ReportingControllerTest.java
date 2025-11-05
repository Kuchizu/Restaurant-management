package ru.ifmo.se.restaurant.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ifmo.se.restaurant.BaseIntegrationTest;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class ReportingControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetRevenue() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/reports/revenue")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenue").exists());
    }

    @Test
    void testGetPopularDishes() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/reports/popular-dishes")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popularDishes").exists());
    }

    @Test
    void testGetProfitability() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/reports/profitability")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenue").exists());
    }

    @Test
    void testGetDishesByRevenue() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/reports/dishes-by-revenue")
                .param("page", "0")
                .param("size", "10")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

