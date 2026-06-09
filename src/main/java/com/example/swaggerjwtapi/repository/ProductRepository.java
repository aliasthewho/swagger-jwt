package  com.example.swaggerjwtapi.repository;

import com.example.swaggerjwtapi.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {

    private final ConcurrentHashMap<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public ProductRepository() {
        save(new Product(null, "Laptop", "Laptop de ejemplo", new BigDecimal("3500.00")));
        save(new Product(null, "Mouse", "Mouse inalambrico", new BigDecimal("80.00")));
        save(new Product(null, "Teclado", "Teclado mecanico", new BigDecimal("250.00")));
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public void deleteById(Long id) {
        products.remove(id);
    }

    public boolean existsById(Long id) {
        return products.containsKey(id);
    }

    public Product save(Product product) {
        if (product.id() == null) {
           product = new Product(
                   sequence.incrementAndGet(),
                   product.name(),
                   product.description(),
                   product.price()
           );
        }
        products.put(product.id(),  product);
        return product;
    }
}
