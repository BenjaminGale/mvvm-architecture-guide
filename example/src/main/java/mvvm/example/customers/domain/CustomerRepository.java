package mvvm.example.customers.domain;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    List<Customer> findAll();
    Optional<Customer> findById(String id);
    void save(Customer customer);
}
