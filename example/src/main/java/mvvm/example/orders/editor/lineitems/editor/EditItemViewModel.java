package mvvm.example.orders.editor.lineitems.editor;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.orders.domain.LineItem;
import mvvm.example.orders.requests.EditItemRequest;

import java.math.BigDecimal;

public class EditItemViewModel {

    private final EditItemRequest request;

    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();

    public EditItemViewModel(EditItemRequest request) {
        this.request = request;

        description.set(request.getItem().description());
        quantity.set(request.getItem().quantity());
        unitPrice.set(request.getItem().unitPrice());
    }

    public void confirm() {
        request.confirmChanges(
            new LineItem(
                request.getItem().productId(),
                description.get(),
                quantity.get(),
                request.getItem().quantityAllocated(),
                unitPrice.get()
            )
        );
    }

    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
}
