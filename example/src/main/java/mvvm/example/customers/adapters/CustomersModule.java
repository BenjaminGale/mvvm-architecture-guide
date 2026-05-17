package mvvm.example.customers.adapters;

import mvvm.example.core.view.DialogManager;
import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.editor.EditCustomerRequest;
import mvvm.example.customers.editor.CustomerEditorService;
import mvvm.example.customers.editor.CustomerEditorView;
import mvvm.example.customers.editor.CustomerEditorViewModel;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersExplorerViewModel;
import mvvm.example.shell.ShellContext;
import mvvm.example.shell.main.sidebar.SidebarItemViewModel;

public class CustomersModule {

    private final ShellContext shell;
    private final DialogManager dialogManager;
    private final CustomerRepository customerRepository;

    public CustomersModule(ViewServices view, ShellContext shell) {
        this.shell = shell;
        this.dialogManager = view.dialogManager();
        this.customerRepository = new InMemoryCustomerRepository();

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
            request -> dialogManager.show(editor(request)));
    }

    private CustomerEditorViewModel editor(EditCustomerRequest request) {
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
