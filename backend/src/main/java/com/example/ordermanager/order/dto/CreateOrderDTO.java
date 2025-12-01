package com.example.ordermanager.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateOrderDTO {

    @NotEmpty
    private List<@Valid CreateOrderItemDTO> items; // valida cada item

    public List<CreateOrderItemDTO> getItems() {
        return items;
    }
}
