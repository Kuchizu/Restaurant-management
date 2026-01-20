package ru.ifmo.se.restaurant.kitchen.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;
import ru.ifmo.se.restaurant.kitchen.application.port.in.AddToQueueUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.in.GetQueueUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.in.UpdateQueueStatusUseCase;
import ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.in.web.KitchenController;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KitchenController.class)
@AutoConfigureMockMvc(addFilters = false)
class KitchenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddToQueueUseCase addToQueueUseCase;

    @MockBean
    private GetQueueUseCase getQueueUseCase;

    @MockBean
    private UpdateQueueStatusUseCase updateQueueStatusUseCase;

    @Test
    void addToQueue() throws Exception {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(1L);
        when(addToQueueUseCase.addToQueue(any())).thenReturn(dto);

        mockMvc.perform(post("/api/kitchen/queue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":1,\"orderItemId\":1,\"dishName\":\"Test\",\"quantity\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getActiveQueue() throws Exception {
        when(getQueueUseCase.getActiveQueue()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/kitchen/queue")).andExpect(status().isOk());
    }

    @Test
    void getAllQueue() throws Exception {
        when(getQueueUseCase.getAllQueue()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/kitchen/queue/all")).andExpect(status().isOk());
    }

    @Test
    void getQueueItemById() throws Exception {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(1L);
        when(getQueueUseCase.getQueueItemById(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/kitchen/queue/1")).andExpect(status().isOk());
    }

    @Test
    void updateStatus() throws Exception {
        KitchenQueueDto dto = new KitchenQueueDto();
        dto.setId(1L);
        when(updateQueueStatusUseCase.updateStatus(1L, DishStatus.IN_PROGRESS)).thenReturn(dto);
        mockMvc.perform(put("/api/kitchen/queue/1/status").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllQueueItemsPaginated() throws Exception {
        org.springframework.data.domain.Page<KitchenQueueDto> page = org.springframework.data.domain.Page.empty();
        when(getQueueUseCase.getAllQueueItemsPaginated(0, 20)).thenReturn(page);
        mockMvc.perform(get("/api/kitchen/queue/paged?page=0&size=20")).andExpect(status().isOk());
    }

    @Test
    void getAllQueueItemsSlice() throws Exception {
        org.springframework.data.domain.Slice<KitchenQueueDto> slice = new org.springframework.data.domain.SliceImpl<>(Collections.emptyList());
        when(getQueueUseCase.getAllQueueItemsSlice(0, 20)).thenReturn(slice);
        mockMvc.perform(get("/api/kitchen/queue/infinite-scroll?page=0&size=20")).andExpect(status().isOk());
    }
}
