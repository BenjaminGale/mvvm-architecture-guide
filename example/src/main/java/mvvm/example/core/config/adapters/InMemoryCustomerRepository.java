package mvvm.example.core.config.adapters;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.domain.CustomerStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<UUID, Customer> store = new HashMap<>();

    public InMemoryCustomerRepository() {
        seed();
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Customer customer) {
        store.put(customer.id(), customer);
    }

    public static final UUID ACME_CORP        = UUID.randomUUID();
    public static final UUID GLOBEX_INC       = UUID.randomUUID();
    public static final UUID INITECH          = UUID.randomUUID();
    public static final UUID UMBRELLA_LTD     = UUID.randomUUID();
    public static final UUID SOYLENT_CORP     = UUID.randomUUID();
    public static final UUID CYBERDYNE_SYSTEMS = UUID.randomUUID();

    private void seed() {
        add(ACME_CORP,         "Acme Corp",         "orders@acme.example",        CustomerStatus.ACTIVE);
        add(GLOBEX_INC,        "Globex Inc",         "purchasing@globex.example",  CustomerStatus.ACTIVE);
        add(INITECH,           "Initech",            "supplies@initech.example",   CustomerStatus.ACTIVE);
        add(UMBRELLA_LTD,      "Umbrella Ltd",       "procurement@umbrella.example", CustomerStatus.ACTIVE);
        add(SOYLENT_CORP,      "Soylent Corp",       "orders@soylent.example",     CustomerStatus.INACTIVE);
        add(CYBERDYNE_SYSTEMS, "Cyberdyne Systems",  "supply@cyberdyne.example",   CustomerStatus.INACTIVE);
    }

    private void add(UUID id, String name, String email, CustomerStatus status) {
        var customer = new Customer(id, name, email, status);
        store.put(customer.id(), customer);
    }
}
