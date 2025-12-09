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
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;

@Tag(name = "Pedidos", description = "Operações de criação, listagem, atualização e cancelamento de pedidos.")
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
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
        @Operation(summary = "Criar pedido",
                description = "Cria um novo pedido com itens e retorna o resumo com total.")
        @PostMapping
        public ResponseEntity<OrderResponseDTO> create(@RequestBody @Valid CreateOrderDTO dto) {
            OrderResponseDTO response = orderService.create(dto);
            return ResponseEntity.ok(response);
        }


    // ================================
    // BUSCAR POR ID
    // ================================
        @PreAuthorize("hasAnyRole('USER','ADMIN')")
        @Operation(summary = "Buscar pedido por ID")
        @GetMapping("/{id}")
        public OrderResponseDTO findById(@PathVariable Long id) {
            return orderService.findById(id);
        }

    // ==============================
    // LISTAR COM PAGINAÇÃO (COM FILTRO OPCIONAL DE STATUS)
    // ==============================
    // Lista pedidos com filtro opcional por status (PENDING, PAID, CANCELED)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Listar pedidos",
               description = "Lista pedidos paginados, com filtro opcional por status (PENDING, PAID, CANCELED).")
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar status do pedido",
               description = "Atualiza o status do pedido para PENDING, PAID ou CANCELED.")
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
    }

    // ================================
    // DELETAR / CANCELAR PEDIDO
    // ================================
    @PreAuthorize("hasRole('ADMIN')")
     @Operation(summary = "Cancelar pedido",
               description = "Cancela um pedido existente, removendo-o da listagem.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
