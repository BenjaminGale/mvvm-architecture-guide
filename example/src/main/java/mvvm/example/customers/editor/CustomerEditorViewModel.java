package mvvm.example.customers.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.customers.domain.Customer;
import mvvm.example.customers.domain.CustomerStatus;

import java.util.UUID;

public class CustomerEditorViewModel {

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final ObjectProperty<CustomerStatus> status = new SimpleObjectProperty<>();

    private final Action confirmAction;
    private final CustomerEditorRequest request;
    private final CustomerEditorService service;

    public CustomerEditorViewModel(CustomerEditorRequest request, CustomerEditorService service) {
        this.request = request;
        this.service = service;

        if (!request.isNew()) {
            var customer = service.load(request.customerId());
            name.set(customer.name());
            email.set(customer.email());
            status.set(customer.status());
        }

        confirmAction = new Action(
            this::confirm,
            name.isNotEmpty()
                .and(email.isNotEmpty())
                .and(status.isNotNull())
        );
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public ObjectProperty<CustomerStatus> statusProperty() {
        return status;
    }

    public Action confirmAction() { return confirmAction; }

    public void confirm() {
        var id = request.isNew() ? UUID.randomUUID() : request.customerId();
        service.save(new Customer(id, name.get(), email.get(), status.get()));
        request.onSaved().run();
    }

    public boolean isNew() {
        return request.isNew();
    }
}
