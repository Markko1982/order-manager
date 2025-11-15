package com.example.ordermanager.controller;

import com.example.ordermanager.order.OrderService;
import com.example.ordermanager.order.OrderStatus;
import com.example.ordermanager.order.dto.CreateOrderDTO;
import com.example.ordermanager.order.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ============================
    // CRIAR PEDIDO
    // ============================
    @PostMapping
    public OrderResponseDTO create(@RequestBody @Valid CreateOrderDTO dto) {
        return orderService.create(dto);
    }

    // ============================
    // BUSCAR POR ID
    // ============================
    @GetMapping("/{id}")
    public OrderResponseDTO findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    // ============================
    // LISTAR COM PAGINAÇÃO
    // ============================
    @GetMapping
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return orderService.findAll(pageable);
    }

    // ============================
    // ATUALIZAR STATUS
    // ============================
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id,
                             @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
    }

    // ============================
    // DELETAR / CANCELAR PEDIDO
    // ============================
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        orderService.delete(id);
    }
}
