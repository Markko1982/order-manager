package com.example.ordermanager.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerAuthTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CategoryRepository categoryRepository;

    private Long categoryId;

    @BeforeEach
    void setUp() {
        Category c = new Category();
        c.setName("Categoria Seed");
        categoryId = categoryRepository.save(c).getId();
    }

    // -------- Sem autenticação -> 403 --------

    @Test
    void get_categories_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_category_by_id_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isForbidden());
    }

    @Test
    void post_categories_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Categoria Teste")))
                .andExpect(status().isForbidden());
    }

    @Test
    void put_categories_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Categoria Atualizada")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_categories_sem_autenticacao_deve_retornar_403() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isForbidden());
    }

    // -------- Leitura -> USER e ADMIN = 200 --------

    @WithMockUser(roles = "USER")
    @Test
    void get_categories_com_user_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void get_categories_com_admin_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "USER")
    @Test
    void get_category_by_id_com_user_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void get_category_by_id_com_admin_deve_retornar_200() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk());
    }

    // -------- Escrita -> USER = 403, ADMIN = sucesso --------

    @WithMockUser(roles = "USER")
    @Test
    void post_categories_com_user_deve_retornar_403() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Categoria X")))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void post_categories_com_admin_deve_retornar_sucesso() throws Exception {
        // aqui vamos validar o status real do controller
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Categoria Nova")))
                .andExpect(status().is2xxSuccessful());
    }

    @WithMockUser(roles = "USER")
    @Test
    void put_categories_com_user_deve_retornar_403() throws Exception {
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Categoria Atualizada")))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void put_categories_com_admin_deve_retornar_sucesso() throws Exception {
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson("Categoria Atualizada")))
                .andExpect(status().is2xxSuccessful());
    }

    @WithMockUser(roles = "USER")
    @Test
    void delete_categories_com_user_deve_retornar_403() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void delete_categories_com_admin_deve_retornar_sucesso() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().is2xxSuccessful());
    }

    private String categoryJson(String name) {
        return """
                { "name": "%s" }
                """.formatted(name);
    }
}
