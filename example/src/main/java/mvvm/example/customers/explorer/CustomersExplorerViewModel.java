package mvvm.example.customers.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.editor.EditCustomerRequest;

import java.util.Comparator;

public class CustomersExplorerViewModel {

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final CustomerService customerService;
    private final CustomerExplorerHost host;

    public CustomersExplorerViewModel(CustomerService customerService, CustomerExplorerHost host) {
        this.customerService = customerService;
        this.host = host;
        load();
    }

    public ObservableList<Customer> getCustomers() {
        return customers;
    }

    public void openCustomer(Customer customer) {
        if (customer != null) host.editCustomer(new EditCustomerRequest(customer.id(), this::refresh));
    }

    private void refresh() {
        load();
    }

    private void load() {
        var sorted = customerService.fetchActive()
            .stream()
            .sorted(Comparator.comparing(Customer::name))
            .toList();
        customers.setAll(sorted);
    }
}
