package mvvm.example.customers.explorer;

import mvvm.example.customers.domain.Customer;

import java.util.List;

public interface CustomersExplorerService {
    List<Customer> fetchCustomers();
}
