package ru.ifmo.se.restaurant.dataaccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.exception.ResourceNotFoundException;
import ru.ifmo.se.restaurant.model.DishStatus;
import ru.ifmo.se.restaurant.model.entity.KitchenQueue;
import ru.ifmo.se.restaurant.model.entity.Order;
import ru.ifmo.se.restaurant.repository.KitchenQueueRepository;
import ru.ifmo.se.restaurant.repository.OrderRepository;

import java.util.List;

@Component
public class KitchenDataAccess {
    private final KitchenQueueRepository kitchenQueueRepository;
    private final OrderRepository orderRepository;
    public KitchenDataAccess(KitchenQueueRepository kitchenQueueRepository, OrderRepository orderRepository) {
        this.kitchenQueueRepository = kitchenQueueRepository;
        this.orderRepository = orderRepository;
    }
    public KitchenQueue saveKitchenQueue(KitchenQueue queue) { return kitchenQueueRepository.save(queue); }
    public List<KitchenQueue> findByStatusesOrderByCreatedAtAsc(List<DishStatus> statuses) { return kitchenQueueRepository.findByStatusesOrderByCreatedAtAsc(statuses); }
    public KitchenQueue findKitchenQueueById(Long id) {
        return kitchenQueueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Kitchen queue item not found with id: " + id));
    }
    public List<KitchenQueue> findByOrderId(Long orderId) { return kitchenQueueRepository.findByOrderId(orderId); }
    public Page<KitchenQueue> findAllKitchenQueues(Pageable pageable) { return kitchenQueueRepository.findAll(pageable); }
    public Order saveOrder(Order order) { return orderRepository.save(order); }
}
