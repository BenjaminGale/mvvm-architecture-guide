package mvvm.example.orders.editor.lineitems;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.orders.domain.LineItem;

import java.math.BigDecimal;

public class LineItemRowViewModel {

    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();
    private final ObjectBinding<BigDecimal> total = Bindings.createObjectBinding(() -> {
            var price = unitPrice.get();
            var qty   = quantity.get();
            return price == null ? BigDecimal.ZERO : price.multiply(BigDecimal.valueOf(qty));
        },
        unitPrice, quantity
    );

    public LineItemRowViewModel(LineItem item) {
        description.set(item.description());
        quantity.set(item.quantity());
        unitPrice.set(item.unitPrice());
    }

    public StringProperty descriptionProperty()           { return description; }
    public IntegerProperty quantityProperty()             { return quantity; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
    public ObjectBinding<BigDecimal> totalProperty()      { return total; }

    public LineItem toLineItem() {
        return new LineItem(description.get(), quantity.get(), unitPrice.get());
    }
}
