package mvvm.example.customers.adapters;

import mvvm.example.core.view.ViewLocator;
import mvvm.example.core.viewmodel.ViewModelRouter;
import mvvm.example.customers.detail.CustomerDetailView;
import mvvm.example.customers.detail.CustomerDetailViewModel;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersExplorerViewModel;

public class CustomerModule {

    private final CustomerService customerService;
    private final ViewModelRouter viewModelRouter;

    public CustomerModule(ViewLocator viewLocator, ViewModelRouter viewModelRouter) {
        this.customerService = new CustomerService(new InMemoryCustomerRepository());
        this.viewModelRouter = viewModelRouter;

        viewLocator.register(CustomersExplorerViewModel.class,      CustomersExplorerView::new);
        viewLocator.register(CustomerDetailViewModel.class, CustomerDetailView::new);
    }

    public void routeToCustomers() {
        viewModelRouter.dispatch(customers());
    }

    private CustomersExplorerViewModel customers() {
        return new CustomersExplorerViewModel(
            customerService,
            customer -> viewModelRouter.dispatch(customerDetail(customer))
        );
    }

    private CustomerDetailViewModel customerDetail(Customer customer) {
        return new CustomerDetailViewModel(
            customer,
            () -> viewModelRouter.dispatch(customers())
        );
    }
}
