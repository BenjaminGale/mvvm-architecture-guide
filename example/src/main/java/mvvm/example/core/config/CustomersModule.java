package mvvm.example.core.config;

import javafx.beans.property.ReadOnlyStringWrapper;
import mvvm.example.core.view.ViewServices;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerRepository;
import mvvm.example.customers.editor.CustomerEditorRequest;
import mvvm.example.customers.editor.CustomerEditorService;
import mvvm.example.customers.editor.CustomerEditorDialog;
import mvvm.example.customers.editor.CustomerEditorViewModel;
import mvvm.example.customers.explorer.CustomersExplorerView;
import mvvm.example.customers.explorer.CustomersExplorerViewModel;
import mvvm.example.shell.tabs.TabContentViewModel;
import mvvm.example.shell.toolbar.ToolbarItem;
import mvvm.example.shell.tabs.TabViewModel;
import mvvm.example.shell.workspace.WorkspaceViewModel;

import java.util.List;
import java.util.UUID;

public class CustomersModule {

    private static final Object EXPLORER_KEY = new Object();

    private final CustomerRepository customerRepository;
    private final ViewServices view;
    private final WorkspaceViewModel workspace;

    public CustomersModule(CustomerRepository customerRepository, ViewServices view) {
        this.customerRepository = customerRepository;
        this.view = view;

        view.viewLocator().register(CustomersExplorerViewModel.class, CustomersExplorerView::new);
        view.dialogManager().register(CustomerEditorViewModel.class, CustomerEditorDialog::dialog);

        this.workspace = new WorkspaceViewModel("Customers");
        workspace.openTab(EXPLORER_KEY, this::customersExplorerTab);
    }

    public WorkspaceViewModel workspace() {
        return workspace;
    }

    private TabViewModel customersExplorerTab() {
        var vm = new CustomersExplorerViewModel(
            customerRepository::findAll,
            request -> view.dialogManager().show(editor(request)));

        workspace.withToolbarActions(List.of(new ToolbarItem.Sync("Add", vm.addItemAction())));

        return TabViewModel.unclosable(
            new ReadOnlyStringWrapper("Customers").getReadOnlyProperty(),
            new TabContentViewModel(vm)
        );
    }

    private CustomerEditorViewModel editor(CustomerEditorRequest request) {
        return new CustomerEditorViewModel(
            request,
            new CustomerEditorService() {
                @Override public Customer load(UUID id) {
                    return customerRepository.findById(id).orElseThrow();
                }
                @Override public void save(Customer customer) {
                    customerRepository.save(customer);
                }
            }
        );
    }
}
