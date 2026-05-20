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
import mvvm.example.orders.domain.OrderStatus;
import mvvm.example.orders.editor.EditOrderRequest;

import java.time.LocalDate;

public class OrderHeaderViewModel {

    public final Action selectCustomer;

    private final LocalDate createdDate;
    private final OrderStatus status;
    private final ObjectProperty<Customer> selectedCustomer = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> plannedShipDate = new SimpleObjectProperty<>();
    private final StringProperty reference = new SimpleStringProperty();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);

    public OrderHeaderViewModel(EditOrderRequest request, OrderHeaderService service, OrderHeaderHost host) {
        var summary = service.fetch(request);
        createdDate = summary.createdDate();
        status = summary.status();
        selectedCustomer.set(summary.customer());
        plannedShipDate.set(summary.plannedShipDate());
        reference.set(summary.reference());

        selectedCustomer.addListener(obs -> validate());
        plannedShipDate.addListener(obs -> validate());
        reference.addListener(obs -> validate());

        selectCustomer = new Action(() -> host.showCustomerSelector(
            new SelectCustomerRequest(selectedCustomer.get(), selectedCustomer::set)
        ));

        validate();
    }

    private void validate() {
        valid.set(selectedCustomer.get() != null
            && plannedShipDate.get() != null
            && reference.get() != null
            && !reference.get().isBlank()
        );
    }

    public LocalDate createdDate() { return createdDate; }
    public OrderStatus status() { return status; }
    public ObjectProperty<Customer> selectedCustomerProperty() { return selectedCustomer; }
    public ObjectProperty<LocalDate> plannedShipDateProperty() { return plannedShipDate; }
    public StringProperty referenceProperty() { return reference; }
    public ReadOnlyBooleanProperty validProperty() { return valid; }
}
