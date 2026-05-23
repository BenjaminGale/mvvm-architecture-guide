package mvvm.example.core.config.adapters;

import mvvm.example.stock.domain.Product;
import mvvm.example.stock.domain.ProductRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryProductRepository implements ProductRepository {

    public static final UUID WIDGET_A     = UUID.randomUUID();
    public static final UUID WIDGET_B     = UUID.randomUUID();
    public static final UUID GIZMO_X      = UUID.randomUUID();
    public static final UUID SPROCKET     = UUID.randomUUID();
    public static final UUID COG          = UUID.randomUUID();
    public static final UUID REAGENT      = UUID.randomUUID();
    public static final UUID MYSTERY_ITEM = UUID.randomUUID();

    private final Map<UUID, Product> store = new HashMap<>();

    public InMemoryProductRepository() {
        seed();
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Product product) {
        store.put(product.id(), product);
    }

    private void seed() {
        add(WIDGET_A,     "Widget A",     new BigDecimal("9.99"),   150);
        add(WIDGET_B,     "Widget B",     new BigDecimal("24.99"),   80);
        add(GIZMO_X,      "Gizmo X",      new BigDecimal("149.00"),  20);
        add(SPROCKET,     "Sprocket",     new BigDecimal("4.50"),   200);
        add(COG,          "Cog",          new BigDecimal("12.75"),  120);
        add(REAGENT,      "Reagent",      new BigDecimal("1.20"),   500);
        add(MYSTERY_ITEM, "Mystery Item", new BigDecimal("999.00"),   5);
    }

    private void add(UUID id, String name, BigDecimal unitPrice, int quantityInStock) {
        store.put(id, new Product(id, name, unitPrice, quantityInStock));
    }
}
