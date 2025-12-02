package com.example.ordermanager.order;

import org.springframework.security.test.context.support.WithMockUser;
import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@WithMockUser(username = "admin@test.com", roles = "ADMIN")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @Test
    void createOrder_returnsOkAndCalculatesTotal() throws Exception {
        Product p1 = new Product();
        p1.setName("Teclado Mecânico");
        p1.setPrice(new BigDecimal("250.00"));
        p1.setStock(10);
        productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Mouse Gamer");
        p2.setPrice(new BigDecimal("150.00"));
        p2.setStock(5);
        productRepository.save(p2);

        String body = String.format(
                "{\"items\":[{\"productId\":%d,\"quantity\":2},{\"productId\":%d,\"quantity\":1}]}",
                p1.getId(), p2.getId()
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(650.0))
                .andExpect(jsonPath("$.items[0].productId").value(p1.getId().intValue()))
                .andExpect(jsonPath("$.items[1].productId").value(p2.getId().intValue()));
    }

    @Test
    void getOrderNotFound_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Pedido não encontrado"));
    }

    @Test
    void createOrderWithInsufficientStock_returns409Conflict() throws Exception {
        Product p = new Product();
        p.setName("Teclado Mecânico");
        p.setPrice(new BigDecimal("250.00"));
        p.setStock(5); // estoque menor que a quantidade do pedido
        productRepository.save(p);

        String body = String.format(
                "{\"items\":[{\"productId\":%d,\"quantity\":999}]}",
                p.getId()
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Estoque insuficiente para o produto: Teclado Mecânico"));
    }

    @Test
    void updateStatusFlow_pendingToConfirmedToCancelled() throws Exception {
        // cria um pedido direto no repositório
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-STATUS-1");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));
        order = orderRepository.save(order);

        Long id = order.getId();

        // PENDING -> CONFIRMED
        mockMvc.perform(put("/api/orders/" + id + "/status")
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk());

        Order confirmed = orderRepository.findById(id).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, confirmed.getStatus());

        // CONFIRMED -> CANCELLED
        mockMvc.perform(put("/api/orders/" + id + "/status")
                .param("status", "CANCELLED"))
                .andExpect(status().isOk());

        Order cancelled = orderRepository.findById(id).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }
}
