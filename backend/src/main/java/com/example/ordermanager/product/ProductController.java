package com.example.ordermanager.product;

import com.example.ordermanager.product.dto.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;

@Tag(name = "Produtos", description = "Operações para gerenciamento de produtos.")
@RestController
@RequestMapping("/api/products")
public class ProductController {


    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

        // LISTAR PRODUTOS - USER ou ADMIN
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Listar produtos",
               description = "Lista produtos paginados, com filtro opcional por nome.")
    @GetMapping
    public Page<Product> list(@RequestParam(required = false) String name,
                              Pageable pageable) {
        return service.list(name, pageable);
    }

    // BUSCAR POR ID - USER ou ADMIN
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Operation(summary = "Buscar produto por ID")
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.get(id);
    }

    // CRIAR PRODUTO - só ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar produto",
               description = "Cria um novo produto com nome, preço e estoque.")
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid ProductDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    // ATUALIZAR PRODUTO - só ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar produto",
               description = "Atualiza os dados de um produto existente.")
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id,
                          @RequestBody @Valid ProductDTO dto) {
        return service.update(id, dto);
    }

    // DELETAR PRODUTO - só ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir produto",
               description = "Remove um produto pelo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
