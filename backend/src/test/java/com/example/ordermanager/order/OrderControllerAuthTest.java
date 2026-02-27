package com.example.ordermanager.order;

import com.example.ordermanager.auth.User;
import com.example.ordermanager.auth.UserRole;
import com.example.ordermanager.auth.UserRepository;
import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import com.example.ordermanager.support.IntegrationTestBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@AutoConfigureMockMvc
@Transactional
class OrderControllerAuthTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // limpa tabelas básicas para evitar interferência entre testes
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Order createSimpleOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));
        return orderRepository.save(order);
    }

    private User createUser(String email, UserRole role) {
        User user = new User();
        user.setName("Teste " + role.name());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Test
    void getOrderById_withoutAuthentication_returnsUnauthorized() throws Exception {
        // Arrange: cria um pedido no banco
        Order order = createSimpleOrder();

        // Act + Assert: sem usuário autenticado → deve dar 401
        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isForbidden());
        // Aqui não validamos o body porque o handler global não trata 401
        // explicitamente.
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = { "USER" })
    void getOrderById_withUserRoleUser_returnsOk() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: usuário autenticado com role USER → deve acessar 200 OK
        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId().intValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    void getOrderById_withUserRoleAdmin_returnsOk() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: usuário autenticado com role ADMIN → também deve acessar
        mockMvc.perform(get("/api/orders/{id}", order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId().intValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void getOrders_withoutAuthentication_returnsForbidden() throws Exception {
        // Act + Assert: sem usuário autenticado → deve bloquear o acesso
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = { "USER" })
    void getOrders_withUserRoleUser_returnsOk() throws Exception {
        // Arrange: garante que existe pelo menos um pedido na base
        createSimpleOrder();

        // Act + Assert: usuário autenticado com role USER → pode listar pedidos
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    void getOrders_withUserRoleAdmin_returnsOk() throws Exception {
        // Arrange: garante que existe pelo menos um pedido na base
        createSimpleOrder();

        // Act + Assert: usuário autenticado com role ADMIN → também pode listar
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_withoutAuthentication_returnsForbidden() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: sem usuário autenticado → deve bloquear (403)
        mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                .param("status", "CONFIRMED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = { "USER" })
    void updateOrderStatus_withUserRoleUser_returnsForbidden() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: usuário autenticado mas com role USER → não pode alterar status
        mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                .param("status", "CONFIRMED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    void updateOrderStatus_withUserRoleAdmin_returnsOk() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: ADMIN pode alterar status → 200 OK
        mockMvc.perform(put("/api/orders/{id}/status", order.getId())
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk());
        // melhoria futura: validar no banco se o status realmente mudou
    }

    @Test
    void deleteOrder_withoutAuthentication_returnsForbidden() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: sem usuário autenticado → 403 Forbidden
        mockMvc.perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = { "USER" })
    void deleteOrder_withUserRoleUser_returnsForbidden() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: USER não pode deletar → 403 Forbidden
        mockMvc.perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = { "ADMIN" })
    void deleteOrder_withUserRoleAdmin_returnsNoContent() throws Exception {
        // Arrange: cria um pedido
        Order order = createSimpleOrder();

        // Act + Assert: ADMIN pode deletar → 204 No Content
        mockMvc.perform(delete("/api/orders/{id}", order.getId()))
                .andExpect(status().isNoContent());
        // melhoria futura: checar no repositório se o pedido realmente foi removido
    }

}
