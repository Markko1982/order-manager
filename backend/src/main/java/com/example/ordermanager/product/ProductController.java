package com.example.ordermanager.product;

import com.example.ordermanager.product.dto.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // LISTAR PRODUTOS - USER ou ADMIN
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public Page<Product> list(@RequestParam(required = false) String name,
                              Pageable pageable) {
        return service.list(name, pageable);
    }

    // BUSCAR POR ID - USER ou ADMIN
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.get(id);
    }

    // CRIAR PRODUTO - só ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid ProductDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    // ATUALIZAR PRODUTO - só ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id,
                          @RequestBody @Valid ProductDTO dto) {
        return service.update(id, dto);
    }

    // DELETAR PRODUTO - só ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
