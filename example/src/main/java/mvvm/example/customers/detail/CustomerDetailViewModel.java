package mvvm.example.customers.detail;

import mvvm.example.customers.Customer;

public class CustomerDetailViewModel {

    private final Customer customer;
    private final Runnable onBack;

    public CustomerDetailViewModel(Customer customer, Runnable onBack) {
        this.customer = customer;
        this.onBack = onBack;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void back() {
        onBack.run();
    }
}
