package mvvm.example.customers.adapters;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> store = new HashMap<>();

    public InMemoryCustomerRepository() {
        seed();
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    private void seed() {
        add("Acme Corp",         "orders@acme.example");
        add("Globex Inc",        "purchasing@globex.example");
        add("Initech",           "supplies@initech.example");
        add("Umbrella Ltd",      "procurement@umbrella.example");
        add("Soylent Corp",      "orders@soylent.example");
        add("Cyberdyne Systems", "supply@cyberdyne.example");
    }

    private void add(String name, String email) {
        var customer = new Customer(UUID.randomUUID().toString(), name, email);
        store.put(customer.id(), customer);
    }
}
