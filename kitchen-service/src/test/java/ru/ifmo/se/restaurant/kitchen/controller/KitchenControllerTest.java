package ru.ifmo.se.restaurant.kitchen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.service.KitchenService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KitchenController.class)
class KitchenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KitchenService kitchenService;

    @Test
    void addToQueue() throws Exception {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(1L);
        when(kitchenService.addToQueue(any())).thenReturn(dto);

        mockMvc.perform(post("/api/kitchen/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":1,\"orderItemId\":1,\"dishName\":\"Test\",\"quantity\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getActiveQueue() throws Exception {
        when(kitchenService.getActiveQueue()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/kitchen/queue")).andExpect(status().isOk());
    }

    @Test
    void getAllQueue() throws Exception {
        when(kitchenService.getAllQueue()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/kitchen/queue/all")).andExpect(status().isOk());
    }

    @Test
    void getQueueItemById() throws Exception {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(1L);
        when(kitchenService.getQueueItemById(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/kitchen/queue/1")).andExpect(status().isOk());
    }

    @Test
    void updateStatus() throws Exception {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(1L);
        when(kitchenService.updateStatus(1L, DishStatus.IN_PROGRESS)).thenReturn(dto);
        mockMvc.perform(put("/api/kitchen/queue/1/status").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk());
    }

    @Test
    void getQueueByOrderId() throws Exception {
        when(kitchenService.getQueueByOrderId(1L)).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/kitchen/queue/order/1")).andExpect(status().isOk());
    }

    @Test
    void getAllQueueItemsPaginated() throws Exception {
        org.springframework.data.domain.Page<KitchenQueueDto> page = org.springframework.data.domain.Page.empty();
        when(kitchenService.getAllQueueItemsPaginated(0, 20)).thenReturn(page);
        mockMvc.perform(get("/api/kitchen/queue/paged?page=0&size=20")).andExpect(status().isOk());
    }

    @Test
    void getAllQueueItemsSlice() throws Exception {
        org.springframework.data.domain.Slice<KitchenQueueDto> slice = new org.springframework.data.domain.SliceImpl<>(Collections.emptyList());
        when(kitchenService.getAllQueueItemsSlice(0, 20)).thenReturn(slice);
        mockMvc.perform(get("/api/kitchen/queue/infinite-scroll?page=0&size=20")).andExpect(status().isOk());
    }
}
