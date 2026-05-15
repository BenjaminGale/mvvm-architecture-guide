package mvvm.example.customers.explorer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;

import java.util.Comparator;
import java.util.function.Consumer;

public class CustomersExplorerViewModel {

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final Consumer<Customer> onCustomerSelected;

    public CustomersExplorerViewModel(CustomerService customerService, Consumer<Customer> onCustomerSelected) {
        this.onCustomerSelected = onCustomerSelected;

        var sorted = customerService.fetchActive()
            .stream()
            .sorted(Comparator.comparing(Customer::name))
            .toList();

        customers.setAll(sorted);
    }

    public ObservableList<Customer> getCustomers() {
        return customers;
    }

    public void openCustomer(Customer customer) {
        if (customer != null) onCustomerSelected.accept(customer);
    }
}
