package mvvm.example.customers.explorer;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.customers.editor.CustomerEditorRequest;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CustomersExplorerViewModel extends ExplorerViewModel<Customer> {

    private final CustomersExplorerService service;
    private final CustomerExplorerHost host;

    public CustomersExplorerViewModel(CustomersExplorerService service, CustomerExplorerHost host) {
        this.service = service;
        this.host = host;
    }

    @Override
    protected ObservableBooleanValue canDeleteCondition() {
        return new SimpleBooleanProperty(false);
    }

    @Override
    protected CompletableFuture<List<Customer>> fetchItemsAsync() {
        return CompletableFuture.completedFuture(
            service.fetchCustomers()
                .stream()
                .filter(c -> c.status() == CustomerStatus.ACTIVE)
                .sorted(Comparator.comparing(Customer::name))
                .toList()
        );
    }

    @Override
    protected void addItem() {
        host.editCustomer(CustomerEditorRequest.newCustomer(this::notifyUpdated));
    }

    @Override
    protected void editItem(Customer customer) {
        host.editCustomer(CustomerEditorRequest.forCustomer(customer.id(), this::notifyUpdated));
    }

}
