package mvvm.example.orders.editor.edititem;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.orders.domain.LineItem;

import java.math.BigDecimal;

public class EditItemViewModel {

    private final EditItemSession session;

    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();

    public EditItemViewModel(EditItemSession session) {
        this.session = session;

        description.set(session.getItem().description());
        quantity.set(session.getItem().quantity());
        unitPrice.set(session.getItem().unitPrice());
    }

    public void confirm() {
        session.confirmChanges(
            new LineItem(
                description.get(),
                quantity.get(),
                unitPrice.get()
            )
        );
    }

    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
}
