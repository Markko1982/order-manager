package com.example.ordermanager.order;

import com.example.ordermanager.order.dto.CreateOrderDTO;
import com.example.ordermanager.order.dto.CreateOrderItemDTO;
import com.example.ordermanager.order.dto.OrderItemResponseDTO;
import com.example.ordermanager.order.dto.OrderResponseDTO;
import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    // ============================
    // CRIAR PEDIDO
    // ============================
    @Transactional
    public OrderResponseDTO create(CreateOrderDTO dto) {

        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderItemDTO itemDTO : dto.getItems()) {

            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProductId()));

                if (product.getStock() < itemDTO.getQuantity()) {
    throw new IllegalStateException("Estoque insuficiente para o produto: " + product.getName());
                
            }

            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem(order, product, itemDTO.getQuantity(), product.getPrice());
            order.getItems().add(item);

            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        // regra de negócio: valor máximo permitido por pedido
        if (total.compareTo(new java.math.BigDecimal("1000.00")) > 0) {
            throw new IllegalStateException("Valor máximo do pedido excedido. Total calculado: " + total);
        }


        Order saved = orderRepository.save(order);

        return toResponseDTO(saved);
    }

    // ============================
    // BUSCAR POR ID
    // ============================
    public OrderResponseDTO findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        return toResponseDTO(order);
    }

        // ============================
        // LISTAR COM PAGINAÇÃO E FILTRO OPCIONAL POR STATUS
        // ============================
        public Page<OrderResponseDTO> findAll(OrderStatus status, Pageable pageable) {
            Page<Order> page;

            if (status == null) {
                page = orderRepository.findAll(pageable);
            } else {
                page = orderRepository.findByStatus(status, pageable);
            }

            return page.map(this::toResponseDTO);
        }

    // ============================
    // ATUALIZAR STATUS
    // ============================
    @Transactional
        public void updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));

        OrderStatus currentStatus = order.getStatus();

             // Regras de transição:
        // - PENDING   -> CONFIRMED ou CANCELLED
        // - CONFIRMED -> CANCELLED
        // - Se já estiver CANCELLED, não pode mudar mais
        if (currentStatus == OrderStatus.PENDING) {
            if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                throw new IllegalStateException("Transição de status inválida: " + currentStatus + " -> " + newStatus);
            }
        } else if (currentStatus == OrderStatus.CONFIRMED) {
            if (newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.CONFIRMED) {
                throw new IllegalStateException("Transição de status inválida: " + currentStatus + " -> " + newStatus);
            }
        } else {
            // CANCELLED não pode ir para outro status diferente
            if (newStatus != currentStatus) {
                throw new IllegalStateException("Pedido já está " + currentStatus + " e não pode ser alterado.");
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }


    // ============================
    // DELETAR PEDIDO
    // ============================
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Pedido não encontrado");
        }
        orderRepository.deleteById(id);
    }

    // ============================
    // MAPEAMENTO PARA DTO
    // ============================
    private OrderResponseDTO toResponseDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(i -> new OrderItemResponseDTO(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()
                )).collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }
}
