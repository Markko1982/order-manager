package com.example.ordermanager.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderDTO {

    @NotEmpty
    private List<CreateOrderItemDTO> items;

    public List<CreateOrderItemDTO> getItems() {
        return items;
    }
}
