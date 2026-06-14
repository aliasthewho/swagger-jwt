package com.example.swaggerjwtapi.controller;

import com.example.swaggerjwtapi.model.Product;
import com.example.swaggerjwtapi.service.ProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Product> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Product findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product create(@Valid @RequestBody ProductRequest request) {
        Product product = new Product(
                null,
                request.name,
                request.description,
                request.price
        );

        return productService.create(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        Product product = new Product(
                id,
                request.name,
                request.description,
                request.price
        );

        return productService.update(id, product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    public record ProductRequest(
            @NotBlank String name,
            @NotBlank String description,
            @NotNull @PositiveOrZero BigDecimal price
    ) {}

}