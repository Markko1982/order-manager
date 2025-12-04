package com.example.ordermanager.product;

import com.example.ordermanager.product.Product;
import com.example.ordermanager.product.ProductRepository;
import com.example.ordermanager.product.dto.ProductDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;



@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;  

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }
    
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @Test
    void listProducts_returnsOkAndContainsSavedProduct() throws Exception {
        Product p = new Product();
        p.setName("Produto Teste");
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(5);
        productRepository.save(p);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Produto Teste"));
    }
    
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    @Test
    void getProductNotFound_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @WithMockUser(username = "user@test.com", roles = "USER")
@Test
void createProduct_asUser_returnsForbidden() throws Exception {
    ProductDTO dto = new ProductDTO();
    dto.setName("Mouse Gamer");
    dto.setPrice(new BigDecimal("150.00"));
    dto.setStock(10);

    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
}

}
