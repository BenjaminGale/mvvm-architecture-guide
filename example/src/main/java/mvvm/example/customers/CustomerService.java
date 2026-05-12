package mvvm.example.customers;

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

    public Optional<Customer> findById(String id) {
        return repository.findById(id);
    }
}
