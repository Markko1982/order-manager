package com.example.ordermanager.order.dto;

import com.example.ordermanager.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal total;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponseDTO> items;

    public OrderResponseDTO(Long id, String orderNumber, OrderStatus status,
                            BigDecimal total, Instant createdAt,
                            Instant updatedAt, List<OrderItemResponseDTO> items) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItemResponseDTO> getItems() { return items; }
}
