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
import ru.ifmo.se.restaurant.dto.SupplierDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderDto;
import ru.ifmo.se.restaurant.dto.SupplyOrderIngredientDto;
import ru.ifmo.se.restaurant.service.SupplierService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class SupplierControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long supplierId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Test Supplier");
        supplier.setAddress("Test Address");
        supplier.setPhone("123456789");
        supplier.setEmail("supplier@example.com");
        SupplierDto created = supplierService.createSupplier(supplier);
        supplierId = created.getId();
    }

    @Test
    void testCreateSupplier() throws Exception {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("New Supplier");
        supplier.setAddress("New Address");
        supplier.setPhone("987654321");
        supplier.setEmail("new@example.com");

        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(supplier)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Supplier"));
    }

    @Test
    void testGetSupplier() throws Exception {
        mockMvc.perform(get("/api/suppliers/" + supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplierId));
    }

    @Test
    void testGetAllSuppliers() throws Exception {
        mockMvc.perform(get("/api/suppliers")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateSupplier() throws Exception {
        SupplierDto supplier = new SupplierDto();
        supplier.setName("Updated Supplier");
        supplier.setAddress("Updated Address");
        supplier.setPhone("111222333");
        supplier.setEmail("updated@example.com");

        mockMvc.perform(put("/api/suppliers/" + supplierId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(supplier)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Supplier"));
    }

    @Test
    void testDeleteSupplier() throws Exception {
        mockMvc.perform(delete("/api/suppliers/" + supplierId))
                .andExpect(status().isNoContent());
    }
}

