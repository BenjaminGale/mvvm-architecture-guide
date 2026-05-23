package mvvm.example.customers.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    List<Customer> findAll();
    Optional<Customer> findById(UUID id);
    void save(Customer customer);
}
