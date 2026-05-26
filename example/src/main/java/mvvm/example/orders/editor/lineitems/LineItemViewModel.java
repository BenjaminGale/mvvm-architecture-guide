package mvvm.example.orders.editor.lineitems;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mvvm.example.core.viewmodel.Action;
import mvvm.example.orders.domain.LineItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LineItemViewModel {

    public final Action editAction;
    public final Action deleteAction;

    private UUID productId;
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>();

    public LineItemViewModel(
        LineItem lineItem,
        Consumer<LineItemEditorRequest> editHost,
        Supplier<List<LineItem>> currentLineItemsSupplier,
        Consumer<LineItemViewModel> deleteCallback
    ) {
        this.productId = lineItem.productId();
        description.set(lineItem.description());
        quantity.set(lineItem.quantity());
        unitPrice.set(lineItem.unitPrice());
        total.set(lineItem.total());

        editAction = new Action(() ->
            editHost.accept(new LineItemEditorRequest(toLineItem(), currentLineItemsSupplier.get(), this::onEdited))
        );
        deleteAction = new Action(() -> deleteCallback.accept(this));
    }

    private void onEdited(LineItem updated) {
        productId = updated.productId();
        description.set(updated.description());
        quantity.set(updated.quantity());
        unitPrice.set(updated.unitPrice());
        total.set(updated.total());
    }

    public LineItem toLineItem() {
        return new LineItem(productId, description.get(), quantity.get(), unitPrice.get());
    }

    public UUID productId() { return productId; }
    public ReadOnlyStringProperty descriptionProperty() { return description; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ReadOnlyObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
    public ReadOnlyObjectProperty<BigDecimal> totalProperty() { return total; }
}
