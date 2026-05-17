package mvvm.example.customers.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.customers.editor.EditCustomerRequest;

import java.util.Comparator;

public class CustomersExplorerViewModel {

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final CustomersExplorerService service;
    private final CustomerExplorerHost host;

    public CustomersExplorerViewModel(CustomersExplorerService service, CustomerExplorerHost host) {
        this.service = service;
        this.host = host;
        load();
    }

    public ObservableList<Customer> getCustomers() {
        return customers;
    }

    public void openCustomer(Customer customer) {
        if (customer != null) host.editCustomer(EditCustomerRequest.forCustomer(customer.id(), this::refresh));
    }

    public void addCustomer() {
        host.editCustomer(EditCustomerRequest.newCustomer(this::refresh));
    }

    private void refresh() {
        load();
    }

    private void load() {
        var sorted = service.fetchCustomers()
            .stream()
            .filter(c -> c.status() == CustomerStatus.ACTIVE)
            .sorted(Comparator.comparing(Customer::name))
            .toList();
        customers.setAll(sorted);
    }
}
