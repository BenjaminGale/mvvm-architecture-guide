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

    public static final String ACME_CORP = "cust-001";
    public static final String GLOBEX_INC = "cust-002";
    public static final String INITECH = "cust-003";
    public static final String UMBRELLA_LTD = "cust-004";
    public static final String SOYLENT_CORP = "cust-005";
    public static final String CYBERDYNE_SYSTEMS = "cust-006";

    private void seed() {
        add(ACME_CORP, "Acme Corp", "orders@acme.example", CustomerStatus.ACTIVE);
        add(GLOBEX_INC, "Globex Inc", "purchasing@globex.example", CustomerStatus.ACTIVE);
        add(INITECH, "Initech", "supplies@initech.example", CustomerStatus.ACTIVE);
        add(UMBRELLA_LTD, "Umbrella Ltd", "procurement@umbrella.example", CustomerStatus.ACTIVE);
        add(SOYLENT_CORP, "Soylent Corp", "orders@soylent.example", CustomerStatus.INACTIVE);
        add(CYBERDYNE_SYSTEMS, "Cyberdyne Systems", "supply@cyberdyne.example", CustomerStatus.INACTIVE);
    }

    private void add(String id, String name, String email, CustomerStatus status) {
        var customer = new Customer(id, name, email, status);
        store.put(customer.id(), customer);
    }
}
