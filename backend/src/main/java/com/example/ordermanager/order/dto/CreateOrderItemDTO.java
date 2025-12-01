package com.example.ordermanager.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para um item do pedido.
 * Regras:
 * - productId não pode ser nulo
 * - quantity >= 1
 * - quantity <= 50 (limite por item)
 */
public class CreateOrderItemDTO {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    @Max(50)
    private Integer quantity;

    public CreateOrderItemDTO() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
