package com.example.ordermanager.order;

import com.example.ordermanager.order.dto.CreateOrderDTO;
import com.example.ordermanager.order.dto.CreateOrderItemDTO;
import com.example.ordermanager.order.dto.OrderResponseDTO;
import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderItemRepository, productRepository);
    }

    @Test
    void create_withAvailableStock_savesOrderUpdatesStockAndReturnsResponse() {
        Product keyboard = product(1L, "Teclado Mecânico", "250.00", 10);
        Product mouse = product(2L, "Mouse Gamer", "150.00", 5);
        CreateOrderDTO dto = createOrderDTO(
                item(1L, 2),
                item(2L, 1));

        when(productRepository.findById(1L)).thenReturn(Optional.of(keyboard));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mouse));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            order.setOrderNumber("ORD-123");
            order.setCreatedAt(Instant.parse("2026-03-17T10:00:00Z"));
            order.setUpdatedAt(Instant.parse("2026-03-17T10:00:00Z"));
            return order;
        });

        OrderResponseDTO response = orderService.create(dto);

        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("650.00"), response.getTotal());
        assertEquals(2, response.getItems().size());
        assertEquals(8, keyboard.getStock());
        assertEquals(4, mouse.getStock());

        verify(productRepository).save(same(keyboard));
        verify(productRepository).save(same(mouse));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void create_withMissingProduct_throwsEntityNotFoundException() {
        CreateOrderDTO dto = createOrderDTO(item(999L, 1));

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.create(dto));

        assertEquals("Produto não encontrado: 999", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void create_withInsufficientStock_throwsIllegalStateException() {
        Product product = product(10L, "Monitor 24", "900.00", 1);
        CreateOrderDTO dto = createOrderDTO(item(10L, 2));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.create(dto));

        assertEquals("Estoque insuficiente para o produto: Monitor 24", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void create_withTotalAboveLimit_throwsIllegalStateExceptionAndDoesNotPersistOrder() {
        Product product = product(20L, "Notebook", "600.00", 10);
        CreateOrderDTO dto = createOrderDTO(item(20L, 2));

        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.create(dto));

        assertEquals("Valor máximo do pedido excedido. Total calculado: 1200.00", exception.getMessage());
        verify(productRepository).save(same(product));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateStatus_whenPendingToConfirmed_updatesAndSavesOrder() {
        Order order = new Order();
        order.setId(30L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(30L)).thenReturn(Optional.of(order));

        orderService.updateStatus(30L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(same(order));
    }

    @Test
    void updateStatus_whenConfirmedToPending_throwsIllegalStateException() {
        Order order = new Order();
        order.setId(31L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(31L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.updateStatus(31L, OrderStatus.PENDING));

        assertEquals("Transição de status inválida: CONFIRMED -> PENDING", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateStatus_whenCancelledToConfirmed_throwsIllegalStateException() {
        Order order = new Order();
        order.setId(32L);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(32L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderService.updateStatus(32L, OrderStatus.CONFIRMED));

        assertEquals("Pedido já está CANCELLED e não pode ser alterado.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void delete_whenOrderExists_deletesById() {
        when(orderRepository.existsById(40L)).thenReturn(true);

        orderService.delete(40L);

        verify(orderRepository).deleteById(40L);
    }

    @Test
    void delete_whenOrderDoesNotExist_throwsEntityNotFoundException() {
        when(orderRepository.existsById(41L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orderService.delete(41L));

        assertEquals("Pedido não encontrado", exception.getMessage());
        verify(orderRepository, never()).deleteById(41L);
    }

    private Product product(Long id, String name, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        return product;
    }

    private CreateOrderItemDTO item(Long productId, int quantity) {
        CreateOrderItemDTO item = new CreateOrderItemDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private CreateOrderDTO createOrderDTO(CreateOrderItemDTO... items) {
        CreateOrderDTO dto = new CreateOrderDTO();
        ReflectionTestUtils.setField(dto, "items", List.of(items));
        return dto;
    }
}