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
import ru.ifmo.se.restaurant.dto.EmployeeDto;
import ru.ifmo.se.restaurant.model.EmployeeRole;
import ru.ifmo.se.restaurant.model.entity.Employee;
import ru.ifmo.se.restaurant.repository.EmployeeRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
class EmployeeControllerTest extends BaseIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long employeeId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john@example.com");
        employee.setPhone("123456789");
        employee.setRole(EmployeeRole.WAITER);
        employee.setIsActive(true);
        Employee saved = employeeRepository.save(employee);
        employeeId = saved.getId();
    }

    @Test
    void testCreateEmployee() throws Exception {
        EmployeeDto employee = new EmployeeDto();
        employee.setFirstName("Jane");
        employee.setLastName("Smith");
        employee.setEmail("jane@example.com");
        employee.setPhone("987654321");
        employee.setRole(EmployeeRole.CHEF);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void testGetEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId));
    }

    @Test
    void testGetAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void testGetEmployeesByRole() throws Exception {
        mockMvc.perform(get("/api/employees/role/WAITER")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

