package com.example.ordermanager.category;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    public CategoryService(CategoryRepository repo) { this.repo = repo; }

    public List<Category> list() { return repo.findAll(); }
    public Category get(Long id) {
        return repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }
    public Category create(Category c) { return repo.save(c); }
    public Category update(Long id, Category data) {
        Category c = get(id);
        c.setName(data.getName());
        return repo.save(c);
    }
    public void delete(Long id) { repo.delete(get(id)); }
}
