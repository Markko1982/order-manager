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
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;


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
@WithMockUser(username = "admin@test.com", roles = "ADMIN")
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

@Test
void createProduct_withBlankName_returnsBadRequest() throws Exception {
    ProductDTO dto = new ProductDTO();
    dto.setName("   "); // inválido por @NotBlank
    dto.setPrice(new BigDecimal("100.00"));
    dto.setStock(10);

    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").isNotEmpty());
}

@Test
void createProduct_withNegativePrice_returnsBadRequest() throws Exception {
    ProductDTO dto = new ProductDTO();
    dto.setName("Mouse Gamer");
    dto.setPrice(new BigDecimal("-1.00")); // inválido por @DecimalMin("0.0")
    dto.setStock(10);

    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").isNotEmpty());
}

@Test
void createProduct_withNegativeStock_returnsBadRequest() throws Exception {
    ProductDTO dto = new ProductDTO();
    dto.setName("Teclado Mecânico");
    dto.setPrice(new BigDecimal("250.00"));
    dto.setStock(-1); // inválido por @Min(0)

    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").isNotEmpty());
}

@Test
void createProduct_asAdmin_returnsCreatedAndLocation() throws Exception {
    ProductDTO dto = new ProductDTO();
    dto.setName("Headset Gamer");
    dto.setPrice(new BigDecimal("300.00"));
    dto.setStock(15);

    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/api/products/")))
            .andExpect(jsonPath("$.name").value("Headset Gamer"))
            .andExpect(jsonPath("$.price").value(300.0))
            .andExpect(jsonPath("$.stock").value(15));
}



}
