package mvvm.example.customers.adapters;

import mvvm.example.AppContext;
import mvvm.example.customers.detail.CustomerDetailView;
import mvvm.example.customers.detail.CustomerDetailViewModel;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerService;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersExplorerViewModel;
import mvvm.example.shell.ShellContext;

public class CustomersModule {

    private final ShellContext shell;
    private final CustomerService customerService;

    public CustomersModule(AppContext appContext, ShellContext shell) {
        this.shell = shell;
        this.customerService = new CustomerService(new InMemoryCustomerRepository());

        appContext.viewLocator().register(CustomersExplorerViewModel.class, CustomersExplorerView::new);
        appContext.viewLocator().register(CustomerDetailViewModel.class, CustomerDetailView::new);
    }

    public void showExplorer() {
        shell.show(this::customersExplorerViewModel);
    }

    public CustomersExplorerViewModel customersExplorerViewModel() {
        return new CustomersExplorerViewModel(
            customerService,
            customer -> shell.show(() -> customerDetailViewModel(customer))
        );
    }

    private CustomerDetailViewModel customerDetailViewModel(Customer customer) {
        return new CustomerDetailViewModel(
            customer,
            () -> shell.show(this::customersExplorerViewModel)
        );
    }
}
