package mvvm.example.orders.editor.header;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.editor.header.CustomerSelectorRequest;

import java.time.LocalDate;
import java.util.function.Consumer;

public class OrderHeaderViewModel {

    private final Action selectCustomerAction;

    private final LocalDate createdDate;
    private final OrderStatus status;
    private final ObjectProperty<Customer> selectedCustomer = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> plannedShipDate = new SimpleObjectProperty<>();
    private final StringProperty reference = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper();

    public OrderHeaderViewModel(Order order, Customer customer, Consumer<CustomerSelectorRequest> selectCustomerHost) {
        createdDate = order.createdDate();
        status = order.status();
        selectedCustomer.set(customer);
        plannedShipDate.set(order.plannedShipDate());
        reference.set(order.reference());

        valid.bind(
            selectedCustomer
                .isNotNull()
                .and(plannedShipDate.isNotNull())
                .and(Bindings.createBooleanBinding(
                    () -> reference.get() != null && !reference.get().isBlank(),
                    reference
                ))
        );

        selectCustomerAction = new Action(() -> selectCustomerHost.accept(
            new CustomerSelectorRequest(selectedCustomer.get(), selectedCustomer::set)
        ));
    }

    public Action selectCustomerAction() { return selectCustomerAction; }

    public LocalDate createdDate() { return createdDate; }
    public OrderStatus status() { return status; }
    public ObjectProperty<Customer> selectedCustomerProperty() { return selectedCustomer; }
    public ObjectProperty<LocalDate> plannedShipDateProperty() { return plannedShipDate; }
    public StringProperty referenceProperty() { return reference; }
    public ReadOnlyBooleanProperty validProperty() { return valid.getReadOnlyProperty(); }
}
