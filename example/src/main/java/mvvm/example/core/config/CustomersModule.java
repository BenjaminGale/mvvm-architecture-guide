package mvvm.example.core.config;

import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.editor.CustomerEditorRequest;
import mvvm.example.customers.editor.CustomerEditorService;
import mvvm.example.customers.editor.CustomerEditorView;
import mvvm.example.customers.editor.CustomerEditorViewModel;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersExplorerViewModel;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;

public class CustomersModule {

    private final CustomerRepository customerRepository;
    private final ViewServices view;
    private final ShellContext shell;

    public CustomersModule(CustomerRepository customerRepository, ViewServices view, ShellContext shell) {
        this.customerRepository = customerRepository;
        this.view = view;
        this.shell = shell;

        view.viewLocator().register(CustomersExplorerViewModel.class, CustomersExplorerView::new);
        view.dialogManager().register(CustomerEditorViewModel.class, CustomerEditorView::dialog);
    }

    public SidebarItemViewModel sidebarItem() {
        return new SidebarItemViewModel("Customers", this::showExplorer);
    }

    public void showExplorer() {
        shell.show(this::customersExplorerViewModel);
    }

    public CustomersExplorerViewModel customersExplorerViewModel() {
        return new CustomersExplorerViewModel(
            customerRepository::findAll,
            request -> view.dialogManager().show(editor(request)));
    }

    private CustomerEditorViewModel editor(CustomerEditorRequest request) {
        return new CustomerEditorViewModel(
            request,
            new CustomerEditorService() {
                @Override public Customer load(String id) {
                    return customerRepository.findById(id).orElseThrow();
                }
                @Override public void save(Customer customer) {
                    customerRepository.save(customer);
                }
            }
        );
    }
}
