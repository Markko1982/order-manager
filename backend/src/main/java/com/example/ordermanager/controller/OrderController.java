package com.example.ordermanager.controller;

import com.example.ordermanager.order.OrderService;
import com.example.ordermanager.order.OrderStatus;
import com.example.ordermanager.order.dto.CreateOrderDTO;
import com.example.ordermanager.order.dto.OrderResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageImpl;
import java.util.List;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ================================
    // CRIAR PEDIDO
    // ================================
    @PostMapping
    public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid CreateOrderDTO dto) {
        OrderResponseDTO response = orderService.create(dto);
        Long id = response.getId();
        URI location = URI.create("/api/orders/" + id);
        return ResponseEntity.created(location).body(response);
    }

    // ================================
    // BUSCAR POR ID
    // ================================
    @GetMapping("/{id}")
    public OrderResponseDTO findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    // ==============================
    // LISTAR COM PAGINAÇÃO (COM FILTRO OPCIONAL DE STATUS)
    // ==============================
    @GetMapping
    public Page<OrderResponseDTO> findAll(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        // Busca paginada normal
        Page<OrderResponseDTO> page = orderService.findAll(pageable);

        // Se não foi passado status, devolve tudo igual antes
        if (status == null) {
            return page;
        }

        // Filtra apenas os pedidos com o status desejado
        List<OrderResponseDTO> filtered = page.getContent().stream()
                .filter(order -> order.getStatus() == status)
                .toList();

        // Monta uma nova Page com os resultados filtrados
        return new PageImpl<>(filtered, pageable, filtered.size());
    }


    // ================================
    // ATUALIZAR STATUS
    // ================================
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
    }

    // ================================
    // DELETAR / CANCELAR PEDIDO
    // ================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
