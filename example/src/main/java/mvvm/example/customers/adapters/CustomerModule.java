package mvvm.example.customers.adapters;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.view.ViewRouter;
import mvvm.example.customers.detail.CustomerDetailView;
import mvvm.example.customers.detail.CustomerDetailViewModel;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersViewModel;

public class CustomerModule {

    private final CustomerService customerService;
    private final ViewRouter viewRouter;

    public CustomerModule(ViewLocator viewLocator, ViewRouter viewRouter) {
        this.customerService = new CustomerService(new InMemoryCustomerRepository());
        this.viewRouter      = viewRouter;

        viewLocator.register(CustomersViewModel.class,      CustomersExplorerView::new);
        viewLocator.register(CustomerDetailViewModel.class, CustomerDetailView::new);
    }

    public void routeToCustomers() {
        viewRouter.route(customers());
    }

    private CustomersViewModel customers() {
        return new CustomersViewModel(
            customerService,
            customer -> viewRouter.route(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) {
        return new CustomerDetailViewModel(
            customer,
            () -> viewRouter.route(customers())
        );
    }
}
