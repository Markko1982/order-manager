package com.example.ordermanager.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.example.ordermanager.support.IntegrationTestBase;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc // filtros ativos (segurança ligada)
@Transactional // cada teste roda e faz rollback (padrão pra integração)
class ProductControllerAuthTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        Product p = new Product();
        p.setName("Produto Seed");
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(5);

        productId = productRepository.save(p).getId();
    }

    // -------- Sem autenticação -> 403 --------

    @Test
    void get_products_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_product_by_id_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isForbidden());
    }

    @Test
    void post_products_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Produto Teste", 10.0, 5)))
                .andExpect(status().isForbidden());
    }

    @Test
    void put_products_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Produto Atualizado", 12.0, 7)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_products_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isForbidden());
    }

    // -------- Leitura -> USER e ADMIN = 200 --------

    @WithMockUser(roles = "USER")
    @Test
    void get_products_com_user_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void get_products_com_admin_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "USER")
    @Test
    void get_product_by_id_com_user_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Produto Seed"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void get_product_by_id_com_admin_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Produto Seed"));
    }

    // -------- Escrita -> USER = 403, ADMIN = sucesso --------

    @WithMockUser(roles = "USER")
    @Test
    void post_products_com_user_deve_retornar_403() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Produto X", 10.0, 5)))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void post_products_com_admin_deve_retornar_201_e_location() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Produto Novo", 20.0, 3)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/products/")));
    }

    @WithMockUser(roles = "USER")
    @Test
    void put_products_com_user_deve_retornar_403() throws Exception {
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Produto Atualizado", 12.0, 7)))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void put_products_com_admin_deve_retornar_200() throws Exception {
        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson("Produto Atualizado", 12.0, 7)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Produto Atualizado"));
    }

    @WithMockUser(roles = "USER")
    @Test
    void delete_products_com_user_deve_retornar_403() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void delete_products_com_admin_deve_retornar_204() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());
    }

    private String productJson(String name, double price, int stock) {
        return """
                {
                  "name": "%s",
                  "price": %s,
                  "stock": %s
                }
                """.formatted(name, price, stock);
    }
}
