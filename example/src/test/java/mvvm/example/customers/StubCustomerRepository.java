package mvvm.example.customers;

import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;

import java.util.List;
import java.util.Optional;

public class StubCustomerRepository implements CustomerRepository {

    private final List<Customer> customers;

    public StubCustomerRepository(Customer... customers) {
        this.customers = List.of(customers);
    }

    @Override public List<Customer> findAll() { return customers; }
    @Override public Optional<Customer> findById(String id) { return customers.stream().filter(c -> c.id().equals(id)).findFirst(); }
    @Override public void save(Customer customer) {}
}
