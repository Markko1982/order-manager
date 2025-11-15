package com.example.ordermanager.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateOrderItemDTO {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
