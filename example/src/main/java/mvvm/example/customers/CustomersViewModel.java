package mvvm.example.customers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.function.Consumer;

public class CustomersViewModel {

    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final Consumer<Customer> onCustomerSelected;

    public CustomersViewModel(CustomerService customerService, Consumer<Customer> onCustomerSelected) {
        this.onCustomerSelected = onCustomerSelected;

        var sorted = customerService.fetchAll()
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
