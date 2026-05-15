package mvvm.example.customers.domain;

import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> fetchAll() {
        return repository.findAll();
    }

    public List<Customer> fetchActive() {
        return repository.findAll().stream()
            .filter(c -> c.status() == CustomerStatus.ACTIVE)
            .toList();
    }

    public Optional<Customer> findById(String id) {
        return repository.findById(id);
    }
}
