package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.orders.LineItem;

import java.math.BigDecimal;

public class LineItemRow {

    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();

    public LineItemRow(LineItem item) {
        description.set(item.description());
        quantity.set(item.quantity());
        unitPrice.set(item.unitPrice());
    }

    public StringProperty descriptionProperty()           { return description; }
    public IntegerProperty quantityProperty()             { return quantity; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }

    public LineItem toLineItem() {
        return new LineItem(description.get(), quantity.get(), unitPrice.get());
    }
}
