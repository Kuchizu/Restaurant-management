package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.ifmo.se.restaurant.kitchen.application.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.application.port.in.AddToQueueUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.in.GetQueueUseCase;
import ru.ifmo.se.restaurant.kitchen.application.port.in.UpdateQueueStatusUseCase;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KitchenControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AddToQueueUseCase addToQueueUseCase;

    @Mock
    private GetQueueUseCase getQueueUseCase;

    @Mock
    private UpdateQueueStatusUseCase updateQueueStatusUseCase;

    @InjectMocks
    private KitchenController kitchenController;

    private KitchenQueueDto testDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(kitchenController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();

        testDto = new KitchenQueueDto();
        testDto.setId(1L);
        testDto.setOrderId(100L);
        testDto.setOrderItemId(10L);
        testDto.setDishName("Pizza");
        testDto.setQuantity(2);
        testDto.setStatus(DishStatus.PENDING);
        testDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void addToQueue_ShouldReturn201() throws Exception {
        when(addToQueueUseCase.addToQueue(any())).thenReturn(testDto);

        mockMvc.perform(post("/api/kitchen/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dishName").value("Pizza"));
    }

    @Test
    void getActiveQueue_ShouldReturnList() throws Exception {
        when(getQueueUseCase.getActiveQueue()).thenReturn(Arrays.asList(testDto));

        mockMvc.perform(get("/api/kitchen/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dishName").value("Pizza"));
    }

    @Test
    void getAllQueue_ShouldReturnList() throws Exception {
        when(getQueueUseCase.getAllQueue()).thenReturn(Arrays.asList(testDto));

        mockMvc.perform(get("/api/kitchen/queue/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getQueueItemById_ShouldReturnItem() throws Exception {
        when(getQueueUseCase.getQueueItemById(1L)).thenReturn(testDto);

        mockMvc.perform(get("/api/kitchen/queue/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStatus_ShouldReturnUpdatedItem() throws Exception {
        testDto.setStatus(DishStatus.IN_PROGRESS);
        when(updateQueueStatusUseCase.updateStatus(eq(1L), eq(DishStatus.IN_PROGRESS)))
                .thenReturn(testDto);

        mockMvc.perform(put("/api/kitchen/queue/1/status")
                        .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_ToReady_ShouldReturnUpdatedItem() throws Exception {
        testDto.setStatus(DishStatus.READY);
        when(updateQueueStatusUseCase.updateStatus(eq(1L), eq(DishStatus.READY)))
                .thenReturn(testDto);

        mockMvc.perform(put("/api/kitchen/queue/1/status")
                        .param("status", "READY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    void updateStatus_ToServed_ShouldReturnUpdatedItem() throws Exception {
        testDto.setStatus(DishStatus.SERVED);
        when(updateQueueStatusUseCase.updateStatus(eq(1L), eq(DishStatus.SERVED)))
                .thenReturn(testDto);

        mockMvc.perform(put("/api/kitchen/queue/1/status")
                        .param("status", "SERVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SERVED"));
    }

    @Test
    void addToQueue_ShouldVerifyUseCase() throws Exception {
        when(addToQueueUseCase.addToQueue(any())).thenReturn(testDto);

        mockMvc.perform(post("/api/kitchen/queue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDto)))
                .andExpect(status().isCreated());

        verify(addToQueueUseCase).addToQueue(any(KitchenQueueDto.class));
    }
}
