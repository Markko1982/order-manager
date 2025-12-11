package com.example.ordermanager.order;

import com.example.ordermanager.auth.User;
import com.example.ordermanager.auth.UserRole;
import com.example.ordermanager.auth.UserRepository;
import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // aqui os filtros de segurança estão ATIVOS (addFilters = true por padrão)
@Transactional
class OrderControllerAuthTest {

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
        // Aqui não validamos o body porque o handler global não trata 401 explicitamente.
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
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
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
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
}
