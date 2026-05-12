package mvvm.example.customers.adapters;

import mvvm.example.core.view.ViewRouter;
import mvvm.example.customers.detail.CustomerDetailViewModel;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.explorer.CustomersViewModel;

public class CustomerModule {

    private final CustomerService customerService;
    private final ViewRouter viewRouter;

    public CustomerModule(CustomerService customerService, ViewRouter viewRouter) {
        this.customerService = customerService;
        this.viewRouter = viewRouter;
    }

    public CustomersViewModel customers() {
        return new CustomersViewModel(
            customerService,
            customer -> viewRouter.navigateTo(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) {
        return new CustomerDetailViewModel(
            customer,
            () -> viewRouter.navigateTo(customers())
        );
    }
}
