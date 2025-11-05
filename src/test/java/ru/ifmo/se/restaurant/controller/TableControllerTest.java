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
import ru.ifmo.se.restaurant.dto.TableDto;
import ru.ifmo.se.restaurant.model.TableStatus;
import ru.ifmo.se.restaurant.service.TableManagementService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class TableControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TableManagementService tableService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long tableId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        TableDto table = new TableDto();
        table.setTableNumber(1);
        table.setCapacity(4);
        table.setLocation("Window");
        TableDto created = tableService.createTable(table);
        tableId = created.getId();
    }

    @Test
    void testCreateTable() throws Exception {
        TableDto table = new TableDto();
        table.setTableNumber(2);
        table.setCapacity(6);
        table.setLocation("Center");

        mockMvc.perform(post("/api/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(table)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value(2));
    }

    @Test
    void testGetTable() throws Exception {
        mockMvc.perform(get("/api/tables/" + tableId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tableId));
    }

    @Test
    void testGetAllTables() throws Exception {
        mockMvc.perform(get("/api/tables")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void testGetTablesByStatus() throws Exception {
        mockMvc.perform(get("/api/tables/status/FREE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateTable() throws Exception {
        TableDto table = new TableDto();
        table.setTableNumber(1);
        table.setCapacity(8);
        table.setLocation("VIP");
        table.setStatus(TableStatus.OCCUPIED);

        mockMvc.perform(put("/api/tables/" + tableId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(table)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(8));
    }

    @Test
    void testDeleteTable() throws Exception {
        mockMvc.perform(delete("/api/tables/" + tableId))
                .andExpect(status().isNoContent());
    }
}

