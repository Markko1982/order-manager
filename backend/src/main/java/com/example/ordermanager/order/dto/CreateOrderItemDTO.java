package com.example.ordermanager.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para um item do pedido.
 * Regras:
 * - productId não pode ser nulo
 * - quantity >= 1
 *   (limites máximos são tratados nas regras de negócio, ex: estoque disponível)
 */
public class CreateOrderItemDTO {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
   
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
