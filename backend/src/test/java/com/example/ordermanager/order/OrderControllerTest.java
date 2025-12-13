package com.example.ordermanager.order;

import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

   @Test
void createOrder_returnsCreatedAndCalculatesTotal() throws Exception {
    // Arrange: cria dois produtos em banco
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

    // 2 x 250 + 1 x 150 = 650
    String body = String.format(
            "{\"items\":[{\"productId\":%d,\"quantity\":2},{\"productId\":%d,\"quantity\":1}]}",
            p1.getId(), p2.getId()
    );

    // Act + Assert: chama POST /api/orders e verifica status, Location e total
    mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", Matchers.containsString("/api/orders/")))
            .andExpect(jsonPath("$.total").value(650.0))
            .andExpect(jsonPath("$.items[0].productId").value(p1.getId().intValue()))
            .andExpect(jsonPath("$.items[1].productId").value(p2.getId().intValue()));
}

    @Test
    void createOrder_withInsufficientStock_returnsConflict() throws Exception {
        // Arrange: cria um produto com pouco estoque
        Product p = new Product();
        p.setName("Monitor 24\"");
        p.setPrice(new BigDecimal("800.00"));
        p.setStock(1); // só 1 unidade em estoque
        productRepository.save(p);

        // Tenta comprar mais do que o estoque disponível (5 > 1)
        String body = String.format(
                "{\"items\":[{\"productId\":%d,\"quantity\":5}]}",
                p.getId()
        );

        // Act + Assert: POST /api/orders deve retornar 409 (CONFLICT)
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value(
                        Matchers.containsString(
                                "Estoque insuficiente para o produto: " + p.getName()
                        )
                ));
    }

    @Test
    void updateStatus_fromPendingToConfirmed_returnsOk() throws Exception {
        // Arrange: cria um pedido em estado PENDING
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));
        Order saved = orderRepository.save(order);

        // Act + Assert: faz PUT /api/orders/{id}/status?status=CONFIRMED
        mockMvc.perform(
                        put("/api/orders/{id}/status", saved.getId())
                                .param("status", "CONFIRMED")
                )
                .andExpect(status().isOk());

        // Verifica no banco se o status foi atualizado
        Order updated = orderRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(OrderStatus.CONFIRMED, updated.getStatus());
    }

    @Test
    void updateStatus_fromConfirmedToCancelled_returnsOk() throws Exception {
        // Arrange: cria um pedido em estado CONFIRMED
        Order order = new Order();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(new BigDecimal("200.00"));
        Order saved = orderRepository.save(order);

        // Act + Assert: faz PUT /api/orders/{id}/status?status=CANCELLED
        mockMvc.perform(
                        put("/api/orders/{id}/status", saved.getId())
                                .param("status", "CANCELLED")
                )
                .andExpect(status().isOk());

        // Verifica no banco se o status foi atualizado
        Order updated = orderRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
    }

    @Test
    void updateStatus_fromCancelledToConfirmed_returnsConflict() throws Exception {
        // Arrange: cria um pedido já CANCELLED
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);
        order.setTotalAmount(new BigDecimal("150.00"));
        Order saved = orderRepository.save(order);

        // Act + Assert: tenta mudar para CONFIRMED → deve falhar
        mockMvc.perform(
                        put("/api/orders/{id}/status", saved.getId())
                                .param("status", "CONFIRMED")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value(
                        Matchers.containsString("Pedido já está")
                ));
    }

    @Test
    void updateStatus_nonExistingOrder_returnsNotFound() throws Exception {
        // Arrange: garante um ID que não existe
        Long nonExistingId = 9999L;
        orderRepository.deleteAll(); // só pra ter certeza

        // Act + Assert: PUT /api/orders/{id}/status deve retornar 404
        mockMvc.perform(
                        put("/api/orders/{id}/status", nonExistingId)
                                .param("status", "CONFIRMED")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(
                        Matchers.containsString("Pedido não encontrado")
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // garante permissão pra deletar
    void deleteOrder_existingOrder_returnsNoContent() throws Exception {
        // Arrange: cria um pedido simples no banco
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("99.90"));
        Order saved = orderRepository.save(order);

        // Sanidade: garante que ele existe antes do delete
        assertTrue(orderRepository.existsById(saved.getId()));

        // Act + Assert: DELETE /api/orders/{id} deve retornar 204 (NO CONTENT)
        mockMvc.perform(delete("/api/orders/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        // Verifica que foi removido do banco
        assertFalse(orderRepository.existsById(saved.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // tem permissão, mas o ID não existe
    void deleteOrder_nonExistingOrder_returnsNotFound() throws Exception {
        // Arrange: garante um ID que não existe
        Long nonExistingId = 9999L;
        orderRepository.deleteAll(); // só pra garantir base limpa

        // Act + Assert: deve retornar 404 e corpo padrão de erro
        mockMvc.perform(delete("/api/orders/{id}", nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(
                        Matchers.containsString("Pedido não encontrado")
                ));
    }

    @Test
    @WithMockUser(roles = "USER") // não é ADMIN
    void deleteOrder_withNonAdminUser_returnsForbidden() throws Exception {
        // Arrange: cria um pedido qualquer
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("50.00"));
        Order saved = orderRepository.save(order);

        // Act + Assert: tentativa de DELETE deve retornar 403
        mockMvc.perform(delete("/api/orders/{id}", saved.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value(
                        Matchers.containsString("Access denied")
                ));
    }

    @Test
    void listOrders_withoutStatusFilter_returnsAllOrders() throws Exception {
        // Arrange: cria 3 pedidos com status diferentes
        Order o1 = new Order();
        o1.setStatus(OrderStatus.PENDING);
        o1.setTotalAmount(new BigDecimal("100.00"));
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setStatus(OrderStatus.CONFIRMED);
        o2.setTotalAmount(new BigDecimal("200.00"));
        orderRepository.save(o2);

        Order o3 = new Order();
        o3.setStatus(OrderStatus.CANCELLED);
        o3.setTotalAmount(new BigDecimal("300.00"));
        orderRepository.save(o3);

        // Act + Assert: GET /api/orders sem filtro deve retornar os 3
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    void listOrders_withStatusFilter_returnsOnlyMatchingStatus() throws Exception {
        // Arrange: cria 3 pedidos com status diferentes
        Order o1 = new Order();
        o1.setStatus(OrderStatus.PENDING);
        o1.setTotalAmount(new BigDecimal("100.00"));
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setStatus(OrderStatus.CONFIRMED);
        o2.setTotalAmount(new BigDecimal("200.00"));
        orderRepository.save(o2);

        Order o3 = new Order();
        o3.setStatus(OrderStatus.CANCELLED);
        o3.setTotalAmount(new BigDecimal("300.00"));
        orderRepository.save(o3);

        // Act + Assert: GET /api/orders?status=CONFIRMED deve retornar só o CONFIRMED
        mockMvc.perform(get("/api/orders")
                        .param("status", "CONFIRMED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    void getOrderById_existingOrder_returnsOkWithBasicFields() throws Exception {
        // Arrange: cria um pedido simples no banco
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("150.00"));
        Order saved = orderRepository.save(order);

        // Act + Assert: GET /api/orders/{id} deve retornar 200
        // e conter id, status e total no JSON
        mockMvc.perform(get("/api/orders/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void getOrderById_nonExistingOrder_returnsNotFoundWithErrorBody() throws Exception {
        // Arrange: garante um ID que não existe
        Long nonExistingId = 999999L;
        orderRepository.deleteAll(); // só pra garantir base limpa

        // Act + Assert: deve retornar 404 com body no padrão global (status + error)
        mockMvc.perform(get("/api/orders/{id}", nonExistingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value(
                        Matchers.containsString("Pedido não encontrado")
                ));
    }

    @Test
void createOrder_withEmptyItems_returnsBadRequest() throws Exception {
    // Arrange: corpo com lista de itens vazia
    String body = "{\"items\":[]}";

    // Act + Assert
    mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").isNotEmpty());
}

@Test
void createOrder_withNullProductId_returnsBadRequest() throws Exception {
    // Arrange: item com productId nulo
    String body = "{\"items\":[{\"productId\":null,\"quantity\":1}]}";

    // Act + Assert
    mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").isNotEmpty());
}

@Test
void createOrder_withZeroQuantity_returnsBadRequest() throws Exception {
    // Arrange: item com quantity inválida (0)
    String body = "{\"items\":[{\"productId\":1,\"quantity\":0}]}";

    // Act + Assert
    mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").isNotEmpty());
}

}
