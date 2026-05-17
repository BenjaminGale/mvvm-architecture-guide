package mvvm.example.stock.adapters;

import mvvm.example.stock.domain.Product;
import mvvm.example.stock.domain.ProductRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> store = new HashMap<>();

    public InMemoryProductRepository() {
        seed();
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Product product) {
        store.put(product.id(), product);
    }

    private void seed() {
        add("Widget A", new BigDecimal("9.99"), 150);
        add("Widget B", new BigDecimal("24.99"), 80);
        add("Gizmo X", new BigDecimal("149.00"), 20);
        add("Sprocket", new BigDecimal("4.50"), 200);
        add("Cog", new BigDecimal("12.75"), 120);
        add("Reagent", new BigDecimal("1.20"), 500);
        add("Mystery Item", new BigDecimal("999.00"), 5);
    }

    private void add(String name, BigDecimal unitPrice, int quantityInStock) {
        var product = new Product(UUID.randomUUID().toString(), name, unitPrice, quantityInStock);
        store.put(product.id(), product);
    }
}
