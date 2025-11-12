package com.example.ordermanager.product;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;
    public ProductService(ProductRepository repo) { this.repo = repo; }

    public List<Product> list() { return repo.findAll(); }

    public Product get(Long id) {
        return repo.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product create(Product p) { return repo.save(p); }

    public Product update(Long id, Product data) {
        Product p = get(id);
        p.setName(data.getName());
        p.setPrice(data.getPrice());
        p.setStock(data.getStock());
        return repo.save(p);
    }

    public void delete(Long id) { repo.delete(get(id)); }
}
