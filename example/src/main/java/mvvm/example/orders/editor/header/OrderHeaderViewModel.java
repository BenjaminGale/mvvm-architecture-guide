package mvvm.example.orders.editor.header;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.customers.domain.Customer;
import mvvm.example.orders.domain.Order;
import mvvm.example.orders.requests.SelectCustomerRequest;

import java.time.LocalDate;
import java.util.function.Consumer;

public class OrderHeaderViewModel {

    public final Action selectCustomer;

    private final ObjectProperty<Customer> selectedCustomer = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> orderDate = new SimpleObjectProperty<>();
    private final StringProperty reference = new SimpleStringProperty();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    public OrderHeaderViewModel(Order order, Customer currentCustomer, Consumer<SelectCustomerRequest> selectCustomerHost) {
        selectedCustomer.set(currentCustomer);
        orderDate.set(order.date());
        reference.set(order.reference());

        selectedCustomer.addListener(obs -> validate());
        orderDate.addListener(obs -> validate());
        reference.addListener(obs -> validate());

        selectCustomer = new Action(() -> selectCustomerHost.accept(
            new SelectCustomerRequest(selectedCustomer.get(), selectedCustomer::set)
        ));

        validate();
    }

    private void validate() {
        valid.set(selectedCustomer.get() != null
            && orderDate.get() != null
            && reference.get() != null
            && !reference.get().isBlank()
        );
    }

    public ObjectProperty<Customer> selectedCustomerProperty() { return selectedCustomer; }
    public ObjectProperty<LocalDate> orderDateProperty() { return orderDate; }
    public StringProperty referenceProperty() { return reference; }
    public ReadOnlyBooleanProperty validProperty() { return valid; }

    public Order.Header buildHeader() {
        var customer = selectedCustomer.get();
        return new Order.Header(customer.id(), customer.name(), orderDate.get(), reference.get());
    }
}
