package com.example.swaggerjwtapi.service;

import com.example.swaggerjwtapi.model.Product;
import com.example.swaggerjwtapi.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    public Product create(Product product) {
        Product newProduct = new Product(
                null,
                product.name(),
                product.description(),
                product.price()
        );
        return productRepository.save(newProduct);
    }

    public Product update(Long id, Product product) {
        findById(id);

        Product updatedProduct = new Product(
                id,
                product.name(),
                product.description(),
                product.price()
        );
        return productRepository.save(updatedProduct);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw  new NoSuchElementException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
