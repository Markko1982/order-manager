package com.example.ordermanager.category;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }

    @GetMapping
    public List<Category> list() { return service.list(); }

    @GetMapping("/{id}")
    public Category get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category c) {
        return ResponseEntity.ok(service.create(c));
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category c) {
        return service.update(id, c);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
