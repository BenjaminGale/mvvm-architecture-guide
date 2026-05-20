package mvvm.example.orders.editor.header;

import mvvm.example.customers.domain.Customer;

import java.util.function.Consumer;

public record SelectCustomerRequest(Customer current, Consumer<Customer> onSelected) {

    public void confirmSelection(Customer customer) { onSelected.accept(customer); }
}
