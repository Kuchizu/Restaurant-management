package ru.ifmo.se.restaurant.common.event;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String ORDERS_CREATED = "restaurant.orders.created";
    public static final String ORDERS_SENT_TO_KITCHEN = "restaurant.orders.sent-to-kitchen";
    public static final String KITCHEN_DISH_READY = "restaurant.kitchen.dish-ready";
    public static final String BILLING_GENERATED = "restaurant.billing.generated";
    public static final String BILLING_PAID = "restaurant.billing.paid";
    public static final String FILES_UPLOADED = "restaurant.files.uploaded";
    public static final String INVENTORY_LOW_STOCK = "restaurant.inventory.low-stock";
}
