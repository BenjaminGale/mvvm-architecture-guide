package mvvm.example.customers.explorer;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import mvvm.example.core.viewmodel.ExplorerViewModel;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;
import mvvm.example.customers.requests.EditCustomerRequest;

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
        host.editCustomer(EditCustomerRequest.newCustomer(this::refresh));
    }

    @Override
    protected void editItem(Customer customer) {
        host.editCustomer(EditCustomerRequest.forCustomer(customer.id(), this::refresh));
    }

    @Override
    protected void deleteItem(Customer customer) {
        throw new UnsupportedOperationException();
    }

    private void refresh() {
        fetchItemsAction().executeAsync(Platform::runLater);
    }
}
