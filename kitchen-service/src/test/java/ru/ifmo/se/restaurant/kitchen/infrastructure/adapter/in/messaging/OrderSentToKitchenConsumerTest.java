package ru.ifmo.se.restaurant.kitchen.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.order.OrderSentToKitchenEvent;
import ru.ifmo.se.restaurant.kitchen.application.port.out.KitchenQueueRepository;
import ru.ifmo.se.restaurant.kitchen.domain.entity.KitchenQueue;
import ru.ifmo.se.restaurant.kitchen.domain.valueobject.DishStatus;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSentToKitchenConsumerTest {

    @Mock
    private KitchenQueueRepository kitchenQueueRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderSentToKitchenConsumer consumer;

    private OrderSentToKitchenEvent.KitchenItem testItem;
    private OrderSentToKitchenEvent testPayload;
    private DomainEvent<OrderSentToKitchenEvent> testEvent;

    @BeforeEach
    void setUp() {
        testItem = OrderSentToKitchenEvent.KitchenItem.builder()
                .orderItemId(10L)
                .dishName("Pizza")
                .quantity(2)
                .specialInstructions("No onions")
                .build();

        testPayload = OrderSentToKitchenEvent.builder()
                .orderId(100L)
                .items(Arrays.asList(testItem))
                .build();

        testEvent = DomainEvent.create("ORDER_SENT_TO_KITCHEN", testPayload);
    }

    @Test
    void handleOrderSentToKitchen_ShouldSaveQueueItems() throws Exception {
        String messageJson = "{}";
        TypeFactory typeFactory = mock(TypeFactory.class);

        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructParametricType(DomainEvent.class, OrderSentToKitchenEvent.class))
                .thenReturn(null);
        when(objectMapper.readValue(eq(messageJson), (com.fasterxml.jackson.databind.JavaType) any()))
                .thenReturn(testEvent);

        KitchenQueue savedQueue = KitchenQueue.builder()
                .id(1L)
                .orderId(100L)
                .orderItemId(10L)
                .dishName("Pizza")
                .quantity(2)
                .status(DishStatus.PENDING)
                .build();

        when(kitchenQueueRepository.save(any(KitchenQueue.class))).thenReturn(savedQueue);

        consumer.handleOrderSentToKitchen(messageJson);

        ArgumentCaptor<KitchenQueue> captor = ArgumentCaptor.forClass(KitchenQueue.class);
        verify(kitchenQueueRepository).save(captor.capture());

        KitchenQueue captured = captor.getValue();
        assertEquals(100L, captured.getOrderId());
        assertEquals(10L, captured.getOrderItemId());
        assertEquals("Pizza", captured.getDishName());
        assertEquals(2, captured.getQuantity());
        assertEquals(DishStatus.PENDING, captured.getStatus());
    }

    @Test
    void handleOrderSentToKitchen_ShouldHandleMultipleItems() throws Exception {
        OrderSentToKitchenEvent.KitchenItem item2 = OrderSentToKitchenEvent.KitchenItem.builder()
                .orderItemId(11L)
                .dishName("Burger")
                .quantity(1)
                .build();

        testPayload = OrderSentToKitchenEvent.builder()
                .orderId(100L)
                .items(Arrays.asList(testItem, item2))
                .build();

        testEvent = DomainEvent.create("ORDER_SENT_TO_KITCHEN", testPayload);

        TypeFactory typeFactory = mock(TypeFactory.class);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructParametricType(DomainEvent.class, OrderSentToKitchenEvent.class))
                .thenReturn(null);
        when(objectMapper.readValue(any(String.class), (com.fasterxml.jackson.databind.JavaType) any()))
                .thenReturn(testEvent);

        when(kitchenQueueRepository.save(any(KitchenQueue.class))).thenAnswer(inv -> inv.getArgument(0));

        consumer.handleOrderSentToKitchen("{}");

        verify(kitchenQueueRepository, times(2)).save(any(KitchenQueue.class));
    }

    @Test
    void handleOrderSentToKitchen_ShouldHandleException() throws Exception {
        when(objectMapper.getTypeFactory()).thenThrow(new RuntimeException("Parse error"));

        // Should not throw - exception is caught and logged
        assertDoesNotThrow(() -> consumer.handleOrderSentToKitchen("invalid"));

        verify(kitchenQueueRepository, never()).save(any());
    }
}
