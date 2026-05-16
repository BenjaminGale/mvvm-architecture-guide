package mvvm.example.customers.adapters;

import mvvm.example.core.view.ViewServices;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;
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

    public CustomersModule(ViewServices view, ShellContext shell) {
        this.shell = shell;
        this.customerService = new CustomerService(new InMemoryCustomerRepository());

        view.viewLocator().register(CustomersExplorerViewModel.class, CustomersExplorerView::new);
        view.viewLocator().register(CustomerDetailViewModel.class, CustomerDetailView::new);
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Customers", this::showExplorer);
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
