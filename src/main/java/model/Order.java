package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Order {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String orderNumber;
    private final LocalDateTime timestamp;
    private final String eventDetails;
    private final Integer quantity;
    private final double totalPrice;
    private final String userId;

    public Order(String orderNumber, LocalDateTime timestamp,
                 String eventDetails, Integer quantity, double totalPrice) {
        this(orderNumber, timestamp, eventDetails, quantity, totalPrice, null);
    }

    public Order(String orderNumber, LocalDateTime timestamp,
                 String eventDetails, Integer quantity, double totalPrice,
                 String userId) {
        this.orderNumber = orderNumber;
        this.timestamp   = timestamp;
        this.eventDetails = eventDetails;
        this.quantity    = quantity;
        this.totalPrice  = totalPrice;
        this.userId      = userId;
    }

    public String getOrderNumber() { return orderNumber; }

    public String getTimestamp() { return timestamp.format(DISPLAY_FORMAT); }

    public String getTitle() { return eventDetails; }

    public Integer getQuantity() { return quantity; }

    public double getPrice() { return totalPrice; }

    public String getUserId() { return userId; }
}
