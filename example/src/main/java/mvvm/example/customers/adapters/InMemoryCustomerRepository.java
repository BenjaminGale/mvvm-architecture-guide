package mvvm.example.customers.adapters;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.domain.CustomerStatus;

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

    @Override
    public void save(Customer customer) {
        store.put(customer.id(), customer);
    }

    private void seed() {
        add("Acme Corp", "orders@acme.example", CustomerStatus.ACTIVE);
        add("Globex Inc", "purchasing@globex.example", CustomerStatus.ACTIVE);
        add("Initech", "supplies@initech.example", CustomerStatus.ACTIVE);
        add("Umbrella Ltd", "procurement@umbrella.example", CustomerStatus.ACTIVE);
        add("Soylent Corp", "orders@soylent.example", CustomerStatus.INACTIVE);
        add("Cyberdyne Systems", "supply@cyberdyne.example", CustomerStatus.INACTIVE);
    }

    private void add(String name, String email, CustomerStatus status) {
        var customer = new Customer(UUID.randomUUID().toString(), name, email, status);
        store.put(customer.id(), customer);
    }
}
