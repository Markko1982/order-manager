package com.example.ordermanager.order;

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
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void createOrder_returnsOkAndCalculatesTotal() throws Exception {
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

        // Act + Assert: chama POST /api/orders e verifica total calculado
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(650.0))
                .andExpect(jsonPath("$.items[0].productId").value(p1.getId().intValue()))
                .andExpect(jsonPath("$.items[1].productId").value(p2.getId().intValue()));
    }
}
