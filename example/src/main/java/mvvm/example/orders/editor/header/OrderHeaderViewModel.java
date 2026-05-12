package mvvm.example.orders.editor.header;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.orders.Order;

import java.time.LocalDate;

public class OrderHeaderViewModel {

    private final StringProperty customerName = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> orderDate = new SimpleObjectProperty<>();
    private final StringProperty reference = new SimpleStringProperty();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    public OrderHeaderViewModel(Order order) {
        customerName.set(order.customerName());
        orderDate.set(order.date());
        reference.set(order.reference());

        customerName.addListener(obs -> validate());
        orderDate.addListener(obs -> validate());
        reference.addListener(obs -> validate());

        validate();
    }

    private void validate() {
        valid.set(
            customerName.get() != null && !customerName.get().isBlank() &&
            orderDate.get() != null &&
            reference.get() != null && !reference.get().isBlank()
        );
    }

    public StringProperty customerNameProperty()         { return customerName; }
    public ObjectProperty<LocalDate> orderDateProperty() { return orderDate; }
    public StringProperty referenceProperty()            { return reference; }
    public ReadOnlyBooleanProperty validProperty()       { return valid; }

    public Order.OrderHeader buildHeader() {
        return new Order.OrderHeader(customerName.get(), orderDate.get(), reference.get());
    }
}
