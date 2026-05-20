package mvvm.example.orders.editor.header;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;

import java.util.List;

public class CustomerSelectorViewModel {

    private final StringProperty searchText = new SimpleStringProperty(this, "searchText", "");
    private final ObjectProperty<Customer> selectedCustomer = new SimpleObjectProperty<>(this, "selectedCustomer");
    private final FilteredList<Customer> customers;
    private final SelectCustomerRequest request;

    public CustomerSelectorViewModel(SelectCustomerRequest request, List<Customer> allCustomers) {
        this.request = request;
        var active = allCustomers.stream().filter(c -> c.status() == CustomerStatus.ACTIVE).toList();
        this.customers = new FilteredList<>(FXCollections.observableArrayList(active));

        selectedCustomer.set(request.current());

        searchText.addListener((_, _, searchText) ->
            customers.setPredicate(customer ->
                searchText == null ||
                searchText.isBlank() ||
                customer
                    .name()
                    .toLowerCase()
                    .contains(searchText.toLowerCase())
            )
        );
    }

    public void confirm() {
        if (selectedCustomer.get() != null) {
            request.confirmSelection(selectedCustomer.get());
        }
    }

    public StringProperty searchTextProperty() { return searchText; }
    public ObjectProperty<Customer> selectedCustomerProperty() { return selectedCustomer; }
    public FilteredList<Customer> getCustomers() { return customers; }
}
