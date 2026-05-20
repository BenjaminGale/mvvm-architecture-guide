package mvvm.example.orders.editor.header;

import mvvm.example.customers.domain.Customer;

import java.util.function.Consumer;

public class SelectCustomerRequest {

    private final Customer current;
    private final Consumer<Customer> onSelected;

    public SelectCustomerRequest(Customer current, Consumer<Customer> onSelected) {
        this.current = current;
        this.onSelected = onSelected;
    }

    public Customer getCurrent() { return current; }

    public void confirmSelection(Customer customer) { onSelected.accept(customer); }
}
